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
        if (togglePause.isPressed()) {
            if (!DiscordIntegration.activated) {
                BugLogger.logChat("MarketShark is not activated! Please enter your Activation Key in /ms and click Connect!", true);
                return;
            }
            Main.paused = !Main.paused;
            BugLogger.logChat((Main.paused ? "Paused" : "Unpaused") + " MarketShark", true);
        }
        if (toggleAntiRender.isPressed()) {
            if (!DiscordIntegration.activated) {
                BugLogger.logChat("MarketShark is not activated! Please enter your Activation Key in /ms and click Connect!", true);
                return;
            }
            if (Main.paused) {
                BugLogger.logChat("MarketShark is Paused! Features will not work until unpaused!", true);
            } else {
                FlipConfig.antiRender = !FlipConfig.antiRender;
                BugLogger.logChat((FlipConfig.antiRender ? "Enabled" : "Disabled") + " AntiRender!", true);
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
