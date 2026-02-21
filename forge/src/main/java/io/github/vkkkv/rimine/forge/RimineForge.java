package io.github.vkkkv.rimine.forge;

import io.github.vkkkv.rimine.core.RimeInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.nio.file.Path;

@Mod("rimine")
public class RimineForge {
    public RimineForge() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        MinecraftForge.EVENT_BUS.register(this);
        
        // JVM shutdown hook as a fallback to guarantee native cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(RimeInputHandler::cleanup));
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        Path configDir = Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("rimine");
        RimeInputHandler.init(configDir.resolve("shared"), configDir.resolve("user"));
    }
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

        int x = 10;
        int y = event.getWindow().getGuiScaledHeight() - 20;

        String text = (data.composition() != null ? data.composition() : "") + " " + data.candidates();
        event.getGuiGraphics().drawString(Minecraft.getInstance().font, text, x, y, 0xFFFFFF, true);
    }
}
