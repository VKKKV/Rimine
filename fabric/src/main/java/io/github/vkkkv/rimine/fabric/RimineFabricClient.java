package io.github.vkkkv.rimine.fabric;

import io.github.vkkkv.rimine.core.RimeInputHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.MinecraftClient;
import java.nio.file.Path;

public class RimineFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            Path configDir = client.runDirectory.toPath().resolve("config").resolve("rimine");
            RimeInputHandler.init(configDir.resolve("shared"), configDir.resolve("user"));
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            RimeInputHandler.cleanup();
        });

        // Intercept keys in screens (like ChatScreen)
        ScreenKeyboardEvents.ALLOW_KEY_PRESS.register((screen, key, scancode, modifiers) -> {
            // If Rime handles the key, we don't allow vanilla to process it.
            return !RimeInputHandler.handleKeyPress(key, modifiers);
        });

        // Render RIME candidates on the HUD
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            RimeInputHandler.RimeData data = RimeInputHandler.getCurrentData();
            if (data == null) return;

            MinecraftClient client = MinecraftClient.getInstance();
            int x = 10;
            int y = client.getWindow().getScaledHeight() - 20;

            String text = (data.composition() != null ? data.composition() : "") + " " + data.candidates();
            drawContext.drawText(client.textRenderer, text, x, y, 0xFFFFFF, true);
        });
    }
}
