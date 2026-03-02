package io.github.vkkkv.rimine.forge;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.vkkkv.rimine.core.RimeInputHandler;
import io.github.vkkkv.rimine.core.RimineConfig;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import static net.minecraft.commands.Commands.argument;
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
  private static final String CONFIG_FILE_NAME = "rimine.json";
  private static final int MOD_SHIFT = 0x0001;
  private static final int MOD_CTRL = 0x0002;
  private static final int KEY_SPACE = 32;
  private static final int KEY_GRAVE = 96;
  private boolean suppressNextCharTyped = false;

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
                                      .resolve(CONFIG_FILE_NAME);
                              RimeInputHandler.reload(configPath);
                              context
                                  .getSource()
                                  .sendSystemMessage(
                                      Component.literal("Rimine configuration reloaded."));
                              return 1;
                            }))
                .then(
                    LiteralArgumentBuilder.<CommandSourceStack>literal("mode")
                        .executes(
                            context -> {
                              context
                                  .getSource()
                                  .sendSystemMessage(
                                      Component.literal(
                                          "Rime mode: "
                                              + (RimeInputHandler.isAsciiMode()
                                                  ? "English (ASCII)"
                                                  : "Chinese")));
                              return 1;
                            })
                        .then(
                            LiteralArgumentBuilder.<CommandSourceStack>literal("toggle")
                                .executes(
                                    context -> {
                                      if (RimeInputHandler.toggleAsciiMode()) {
                                        context
                                            .getSource()
                                            .sendSystemMessage(
                                                Component.literal(
                                                    "Rime mode: "
                                                        + (RimeInputHandler.isAsciiMode()
                                                            ? "English (ASCII)"
                                                            : "Chinese")));
                                        return 1;
                                      }
                                      context
                                          .getSource()
                                          .sendSystemMessage(
                                              Component.literal("Failed to toggle Rime mode."));
                                      return 0;
                                    }))
                        .then(
                            LiteralArgumentBuilder.<CommandSourceStack>literal("zh")
                                .executes(
                                    context -> {
                                      boolean ok = RimeInputHandler.setAsciiMode(false);
                                      context
                                          .getSource()
                                          .sendSystemMessage(
                                              Component.literal(
                                                  ok
                                                      ? "Rime mode: Chinese"
                                                      : "Failed to set Chinese mode."));
                                      return ok ? 1 : 0;
                                    }))
                        .then(
                            LiteralArgumentBuilder.<CommandSourceStack>literal("en")
                                .executes(
                                    context -> {
                                      boolean ok = RimeInputHandler.setAsciiMode(true);
                                      context
                                          .getSource()
                                          .sendSystemMessage(
                                              Component.literal(
                                                  ok
                                                      ? "Rime mode: English (ASCII)"
                                                      : "Failed to set English mode."));
                                      return ok ? 1 : 0;
                                    })))
                .then(
                    LiteralArgumentBuilder.<CommandSourceStack>literal("schema")
                        .executes(
                            context -> {
                              String schema = RimeInputHandler.getCurrentSchemaId();
                              context
                                  .getSource()
                                  .sendSystemMessage(
                                      Component.literal(
                                          "Current schema: " + (schema == null ? "N/A" : schema)));
                              return 1;
                            })
                        .then(
                            LiteralArgumentBuilder.<CommandSourceStack>literal("next")
                                .executes(
                                    context -> {
                                      String schema = RimeInputHandler.cycleSchema();
                                      context
                                          .getSource()
                                          .sendSystemMessage(
                                              Component.literal(
                                                  schema != null
                                                      ? "Switched schema: " + schema
                                                      : "Failed to switch schema."));
                                      return schema != null ? 1 : 0;
                                    }))
                        .then(
                            argument("id", StringArgumentType.word())
                                .executes(
                                    context -> {
                                      String schemaId =
                                          StringArgumentType.getString(context, "id");
                                      boolean ok = RimeInputHandler.setSchema(schemaId);
                                      context
                                          .getSource()
                                          .sendSystemMessage(
                                              Component.literal(
                                                  ok
                                                      ? "Switched schema: " + schemaId
                                                      : "Failed to switch schema: " + schemaId));
                                      return ok ? 1 : 0;
                                    }))));
  }

  private void onClientSetup(FMLClientSetupEvent event) {
    Path configDir =
        Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("rimine");
    RimeInputHandler.init(configDir.resolve("shared"), configDir.resolve("user"));
  }

  @SubscribeEvent
  public void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
    if (!(event.getScreen() instanceof ChatScreen)) return;
    suppressNextCharTyped = false;
    if ((event.getModifiers() & (MOD_CTRL | MOD_SHIFT)) == (MOD_CTRL | MOD_SHIFT)) {
      if (event.getKeyCode() == KEY_SPACE) {
        if (RimeInputHandler.toggleAsciiMode()) {
          showOverlayMessage(
              "Rime mode: " + (RimeInputHandler.isAsciiMode() ? "English" : "Chinese"));
        }
        suppressNextCharTyped = true;
        event.setCanceled(true);
        return;
      }
      if (event.getKeyCode() == KEY_GRAVE) {
        String schema = RimeInputHandler.cycleSchema();
        showOverlayMessage(schema != null ? "Schema: " + schema : "Schema switch failed");
        suppressNextCharTyped = true;
        event.setCanceled(true);
        return;
      }
    }
    if (RimeInputHandler.handleKeyPress(event.getKeyCode(), event.getModifiers())) {
      applyCommitToChat((ChatScreen) event.getScreen());
      suppressNextCharTyped = true;
      event.setCanceled(true);
    }
  }

  @SubscribeEvent
  public void onCharacterTyped(ScreenEvent.CharacterTyped.Pre event) {
    if (!(event.getScreen() instanceof ChatScreen)) return;
    if (suppressNextCharTyped) {
      suppressNextCharTyped = false;
      event.setCanceled(true);
      return;
    }
    if (RimeInputHandler.shouldBlockCharTyped(event.getCodePoint())) {
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
    for (int i = 0; i < candidates.size(); i++) {
      String candidateText = "  " + (i + 1) + ". " + candidates.get(i);
      width = Math.max(width, mc.font.width(candidateText));
    }
    width += 6;

    int height =
        (candidates.size()
                + (data.composition() != null ? 1 : 0)
                + (data.isSwitcher() ? 1 : 0)
                + 1)
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

  private void applyCommitToChat(ChatScreen chatScreen) {
    String commitText = RimeInputHandler.consumeCommitText();
    if (commitText == null || commitText.isEmpty()) return;

    Screen screen = chatScreen;
    GuiEventListener focused = screen.getFocused();
    if (focused instanceof EditBox input) {
      input.insertText(commitText);
    }
  }

  private void showOverlayMessage(String message) {
    if (Minecraft.getInstance().player != null) {
      Minecraft.getInstance().player.displayClientMessage(Component.literal(message), true);
    }
  }
}
