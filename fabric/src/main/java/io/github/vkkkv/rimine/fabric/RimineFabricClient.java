package io.github.vkkkv.rimine.fabric;

import io.github.vkkkv.rimine.core.RimeInputHandler;
import io.github.vkkkv.rimine.core.RimineConfig;
import java.nio.file.Path;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class RimineFabricClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    ClientLifecycleEvents.CLIENT_STARTED.register(
        client -> {
          Path configDir = client.gameDirectory.toPath().resolve("config").resolve("rimine");
          RimeInputHandler.init(configDir.resolve("shared"), configDir.resolve("user"));
        });

    ClientCommandRegistrationCallback.EVENT.register(
        (dispatcher, registryAccess) -> {
          dispatcher.register(
              ClientCommandManager.literal("rimine")
                  .then(
                      ClientCommandManager.literal("reload")
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
                                        Component.literal("Rimine configuration reloaded."));
                                return 1;
                              })));
        });

    ClientLifecycleEvents.CLIENT_STOPPING.register(
        client -> {
          RimeInputHandler.cleanup();
        });

    // Intercept keys in all screens
    ScreenEvents.AFTER_INIT.register(
        (client, screen, scaledWidth, scaledHeight) -> {
          ScreenKeyboardEvents.allowKeyPress(screen)
              .register(
                  (s, key, scancode, modifiers) -> {
                    return !RimeInputHandler.handleKeyPress(key, modifiers);
                  });
        });

    // Render RIME candidates on the HUD
    HudRenderCallback.EVENT.register(
        (guiGraphics, tickDelta) -> {
          RimeInputHandler.RimeData data = RimeInputHandler.getCurrentData();
          if (data == null) return;

          RimineConfig config = RimineConfig.get();
          Minecraft client = Minecraft.getInstance();
          int x = data.x() + config.ui_offset_x;
          int y = data.y() + config.ui_offset_y;

          List<String> candidates = data.candidates();
          int lineHeight = client.font.lineHeight + 2;
          int width = 0;

          if (data.composition() != null) {
            width = client.font.width(data.composition());
          }
          for (String cand : candidates) {
            width = Math.max(width, client.font.width(cand) + 20);
          }
          width += 4;

          int height =
              (candidates.size()
                          + (data.composition() != null ? 1 : 0)
                          + (data.isSwitcher() ? 1 : 0)
                          + 1)
                      * lineHeight
                  + 4;

          // Draw background using config
          guiGraphics.fill(x - 2, y - 2, x + width, y + height - 2, config.getResolvedBgColor());

          int currentY = y;
          if (data.isSwitcher()) {
            guiGraphics.drawString(
                client.font, "§6Schema Selection:", x, currentY, 0xFFFFFF, false);
            currentY += lineHeight;
          }

          if (data.composition() != null) {
            guiGraphics.drawString(client.font, data.composition(), x, currentY, 0xEEEEEE, false);
            currentY += lineHeight;
          }

          for (int i = 0; i < candidates.size(); i++) {
            int color = (i == data.highlightedIndex()) ? 0xFFFF55 : 0xBBBBBB;
            String prefix = (i == data.highlightedIndex()) ? "> " : "  ";
            guiGraphics.drawString(
                client.font,
                prefix + (i + 1) + ". " + candidates.get(i),
                x,
                currentY,
                color,
                false);
            currentY += lineHeight;
          }

          // Page indicator
          String pageInfo = "[" + (data.pageNo() + 1) + (data.isLastPage() ? "]" : "+]");
          guiGraphics.drawString(client.font, pageInfo, x, currentY, 0x777777, false);
        });
  }
}
