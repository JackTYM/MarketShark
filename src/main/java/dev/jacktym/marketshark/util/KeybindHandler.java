package dev.jacktym.marketshark.util;

import dev.jacktym.marketshark.Main;
import dev.jacktym.marketshark.config.FlipConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class KeybindHandler {
    public static KeyBinding togglePause;
    public static KeyBinding toggleAntiRender;
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!DiscordIntegration.activated) {
            ChatUtils.printMarkedChat("MarketShark is not activated! Please enter your Activation Key in /ms and click Connect!");
            return;
        }
        if (togglePause.isPressed()) {
            Main.paused = !Main.paused;
            ChatUtils.printMarkedChat((Main.paused ? "Paused" : "Unpaused") + " MarketShark");
        }
        if (toggleAntiRender.isPressed()) {
            if (Main.paused) {
                ChatUtils.printMarkedChat("MarketShark is Paused! Features will not work until unpaused!");
            } else {
                FlipConfig.antiRender = !FlipConfig.antiRender;
                ChatUtils.printMarkedChat((FlipConfig.antiRender ? "Enabled" : "Disabled") + " AntiRender!");
                // Reload chunks
                Minecraft.getMinecraft().renderGlobal.loadRenderers();
            }
        }
    }

    public static void initializeKeybinds() {
        togglePause = new KeyBinding("Toggle Pause", Keyboard.KEY_NONE, "MarketShark");
        toggleAntiRender = new KeyBinding("Toggle AntiRender", Keyboard.KEY_NONE, "MarketShark");

        ClientRegistry.registerKeyBinding(togglePause);
        ClientRegistry.registerKeyBinding(toggleAntiRender);
    }
}
