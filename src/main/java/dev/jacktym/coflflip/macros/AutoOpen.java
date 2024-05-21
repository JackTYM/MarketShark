package dev.jacktym.coflflip.macros;

import dev.jacktym.coflflip.Main;
import dev.jacktym.coflflip.config.FlipConfig;
import dev.jacktym.coflflip.util.CoflWebsocketClient;
import net.minecraft.client.gui.inventory.GuiChest;

public class AutoOpen {
    public static void openAuction() {
        if (!FlipConfig.autoOpen || CoflWebsocketClient.auctionQueue.isEmpty()) {
            return;
        }
        if (Main.mc.thePlayer != null && !(Main.mc.currentScreen instanceof GuiChest)) {
            AutoBuy.autoBuy();
            Main.mc.thePlayer.sendChatMessage(CoflWebsocketClient.auctionQueue.get(0));
        }
    }
}
