package io.github.vkkkv.rimine.forge;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.vkkkv.rimine.core.RimeInputHandler;
import io.github.vkkkv.rimine.core.RimineConfig;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("rimine")
public class RimineForge {
  public RimineForge() {
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    MinecraftForge.EVENT_BUS.register(this);
    MinecraftForge.EVENT_BUS.addListener(this::registerCommands);

    Runtime.getRuntime().addShutdownHook(new Thread(RimeInputHandler::cleanup));
  }

  private void registerCommands(RegisterClientCommandsEvent event) {
    event
        .getDispatcher()
        .register(
            LiteralArgumentBuilder.<CommandSourceStack>literal("rimine")
                .then(
                    LiteralArgumentBuilder.<CommandSourceStack>literal("reload")
                        .executes(
                            context -> {
                              Path configPath =
                                  Minecraft.getInstance()
                                      .gameDirectory
                                      .toPath()
                                      .resolve("config")
                                      .resolve("rimine.json");
                              RimeInputHandler.reload(configPath);
                              context
                                  .getSource()
                                  .sendFeedback(
                                      Component.literal("Rimine configuration reloaded."), false);
                              return 1;
                            })));
  }

  private void onClientSetup(FMLClientSetupEvent event) {
    Path configDir =
        Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("rimine");
    RimeInputHandler.init(configDir.resolve("shared"), configDir.resolve("user"));
  }

  @SubscribeEvent
  public void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
    // Intercept key press
    if (RimeInputHandler.handleKeyPress(event.getKeyCode(), event.getModifiers())) {
      event.setCanceled(true);
    }
  }

  @SubscribeEvent
  public void onRenderGui(RenderGuiOverlayEvent.Post event) {
    RimeInputHandler.RimeData data = RimeInputHandler.getCurrentData();
    if (data == null) return;

    RimineConfig config = RimineConfig.get();
    Minecraft mc = Minecraft.getInstance();
    int x = data.x() + config.ui_offset_x;
    int y = data.y() + config.ui_offset_y;

    List<String> candidates = data.candidates();
    int lineHeight = mc.font.lineHeight + 2;
    int width = 0;

    if (data.composition() != null) {
      width = mc.font.width(data.composition());
    }
    for (String cand : candidates) {
      width = Math.max(width, mc.font.width(cand) + 20);
    }
    width += 4;

    int height =
        (candidates.size() + (data.composition() != null ? 1 : 0) + (data.isSwitcher() ? 1 : 0) + 1)
                * lineHeight
            + 4;

    // Draw background using config
    event
        .getGuiGraphics()
        .fill(x - 2, y - 2, x + width, y + height - 2, config.getResolvedBgColor());

    int currentY = y;
    if (data.isSwitcher()) {
      event
          .getGuiGraphics()
          .drawString(mc.font, "§6Schema Selection:", x, currentY, 0xFFFFFF, false);
      currentY += lineHeight;
    }

    if (data.composition() != null) {
      event.getGuiGraphics().drawString(mc.font, data.composition(), x, currentY, 0xEEEEEE, false);
      currentY += lineHeight;
    }

    for (int i = 0; i < candidates.size(); i++) {
      int color = (i == data.highlightedIndex()) ? 0xFFFF55 : 0xBBBBBB;
      String prefix = (i == data.highlightedIndex()) ? "> " : "  ";
      event
          .getGuiGraphics()
          .drawString(
              mc.font, prefix + (i + 1) + ". " + candidates.get(i), x, currentY, color, false);
      currentY += lineHeight;
    }

    // Page indicator
    String pageInfo = "[" + (data.pageNo() + 1) + (data.isLastPage() ? "]" : "+]");
    event.getGuiGraphics().drawString(mc.font, pageInfo, x, currentY, 0x777777, false);
  }
}
