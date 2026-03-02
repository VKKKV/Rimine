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
  public void onRenderGui(ScreenEvent.Render.Post event) {
    RimeInputHandler.RimeData data = RimeInputHandler.getCurrentData();
    if (data == null) return;

    Minecraft mc = Minecraft.getInstance();
    if (!(mc.screen instanceof ChatScreen chatScreen)) return;
    GuiEventListener focused = chatScreen.getFocused();
    if (!(focused instanceof EditBox editBox)) return;

    RimineConfig config = RimineConfig.get();
    List<String> candidates = data.candidates();
    String composition = data.composition() == null ? "" : data.composition();
    String pageInfo = "[" + (data.pageNo() + 1) + (data.isLastPage() ? "]" : "+]");

    int lineHeight = mc.font.lineHeight + 2;
    int padX = 6;
    int padY = 5;
    int rowGap = 2;

    int contentWidth = mc.font.width(pageInfo);
    if (!composition.isEmpty()) {
      contentWidth = Math.max(contentWidth, mc.font.width(composition));
    }
    if (data.isSwitcher()) {
      contentWidth = Math.max(contentWidth, mc.font.width("Schema Selection"));
    }
    // Include "> " prefix width (widest prefix) so panel accommodates selected state
    for (int i = 0; i < candidates.size(); i++) {
      contentWidth = Math.max(contentWidth, mc.font.width("> " + (i + 1) + ". " + candidates.get(i)));
    }

    int rowCount =
        candidates.size() + 1 + (composition.isEmpty() ? 0 : 1) + (data.isSwitcher() ? 1 : 0);
    int panelWidth = contentWidth + padX * 2;
    int panelHeight = rowCount * lineHeight + padY * 2 - rowGap;

    int screenWidth = mc.getWindow().getGuiScaledWidth();
    int screenHeight = mc.getWindow().getGuiScaledHeight();
    // Anchor left to the EditBox left edge; bottom of panel sits just above it
    int panelX = Math.min(editBox.getX() + config.ui_offset_x, screenWidth - panelWidth - 2);
    panelX = Math.max(2, panelX);
    int panelY = editBox.getY() - panelHeight - editBox.getHeight() - 2 + config.ui_offset_y;
    panelY = Math.max(2, Math.min(panelY, screenHeight - panelHeight - 2));

    // Colors: Minecraft-inspired earth tones with warm accents
    int shadowColor = 0x4D000000;
    int outerBorder = 0xFF1A1A1A;
    int innerBorder = 0xFF555555;
    int dividerColor = 0x6B3B2A14;
    int panelBg = config.getResolvedBgColor();
    int headerBg = 0x663B2A14;
    int compositionBg = 0x55212121;
    int selectedBg = 0x8848752B;
    int selectedText = 0xFFF0F8A0;
    int normalText = 0xFFE0E0E0;
    int metaText = 0xFFAAAAAA;

    // Draw subtle shadow for depth
    event.getGuiGraphics().fill(panelX + 2, panelY + panelHeight, panelX + panelWidth - 1, panelY + panelHeight + 1, shadowColor);
    event.getGuiGraphics().fill(panelX + panelWidth, panelY + 2, panelX + panelWidth + 1, panelY + panelHeight - 1, shadowColor);

    // Draw layered borders for frame effect
    event.getGuiGraphics().fill(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelY + panelHeight + 1, outerBorder);
    event.getGuiGraphics().fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, innerBorder);
    event.getGuiGraphics().fill(panelX + 1, panelY + 1, panelX + panelWidth - 1, panelY + panelHeight - 1, panelBg);

    int currentY = panelY + padY;
    if (data.isSwitcher()) {
      event
          .getGuiGraphics()
          .fill(panelX + 2, currentY - 1, panelX + panelWidth - 2, currentY + mc.font.lineHeight + 1, headerBg);
      event
          .getGuiGraphics()
          .drawString(mc.font, "Schema Selection", panelX + padX, currentY, 0xFFFFA500, true);
      currentY += lineHeight;
      // Subtle divider after header
      event.getGuiGraphics().fill(panelX + 3, currentY - padY + 1, panelX + panelWidth - 3, currentY - padY + 2, dividerColor);
    }

    if (!composition.isEmpty()) {
      event
          .getGuiGraphics()
          .fill(
              panelX + 2,
              currentY - 1,
              panelX + panelWidth - 2,
              currentY + mc.font.lineHeight + 1,
              compositionBg);
      event
          .getGuiGraphics()
          .drawString(mc.font, composition, panelX + padX, currentY, 0xFFFFFFFF, false);
      currentY += lineHeight;
      // Divider after composition
      event.getGuiGraphics().fill(panelX + 3, currentY - padY + 1, panelX + panelWidth - 3, currentY - padY + 2, dividerColor);
    }

    for (int i = 0; i < candidates.size(); i++) {
      boolean selected = i == data.highlightedIndex();
      if (selected) {
        event
            .getGuiGraphics()
            .fill(
                panelX + 2,
                currentY - 1,
                panelX + panelWidth - 2,
                currentY + mc.font.lineHeight + 1,
                selectedBg);
      }
      String prefix = selected ? "> " : "  ";
      int color = selected ? selectedText : normalText;
      event
          .getGuiGraphics()
          .drawString(
              mc.font,
              prefix + (i + 1) + ". " + candidates.get(i),
              panelX + padX,
              currentY,
              color,
              false);
      currentY += lineHeight;
    }

    int pageX = panelX + panelWidth - padX - mc.font.width(pageInfo);
    event.getGuiGraphics().drawString(mc.font, pageInfo, pageX, currentY, metaText, false);
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
