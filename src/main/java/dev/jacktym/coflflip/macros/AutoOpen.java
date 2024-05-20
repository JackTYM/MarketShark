package dev.jacktym.coflflip.macros;

import dev.jacktym.coflflip.Main;
import net.minecraft.client.gui.inventory.GuiChest;

public class AutoOpen {
    public static void openAuction(String viewAuction) {
        if (Main.mc.thePlayer != null && !(Main.mc.currentScreen instanceof GuiChest)) {
            Main.mc.thePlayer.sendChatMessage(viewAuction);
            AutoBuy.autoBuy();
        }
    }
}
