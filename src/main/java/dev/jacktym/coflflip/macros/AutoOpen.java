package dev.jacktym.coflflip.macros;

import dev.jacktym.coflflip.Main;
import dev.jacktym.coflflip.config.FlipConfig;
import net.minecraft.client.gui.inventory.GuiChest;

public class AutoOpen {
    public static void openAuction(String viewAuction) {
        if (!FlipConfig.autoOpen) {
            return;
        }
        if (Main.mc.thePlayer != null && !(Main.mc.currentScreen instanceof GuiChest)) {
            AutoBuy.autoBuy();
            Main.mc.thePlayer.sendChatMessage(viewAuction);
        }
    }
}
