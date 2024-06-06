package dev.jacktym.coflflip.macros;

import dev.jacktym.coflflip.Main;
import dev.jacktym.coflflip.config.FlipConfig;
import dev.jacktym.coflflip.util.FlipItem;
import dev.jacktym.coflflip.util.QueueUtil;
import net.minecraft.client.gui.inventory.GuiChest;

public class AutoOpen {
    public static void openAuction(FlipItem item) {
        if (!FlipConfig.autoOpen) {
            return;
        }
        System.out.println("TRY OPEN");
        QueueUtil.addToQueue(() -> {
            if (FlipConfig.debug) {
                System.out.println("Attempting Open: " + item.auctionId);
            }
            if (Main.mc.thePlayer != null && !(Main.mc.currentScreen instanceof GuiChest)) {
                AutoBuy.autoBuy(item);
                Main.mc.thePlayer.sendChatMessage("/viewauction " + item.auctionId);
            }
            QueueUtil.finishAction();
        });
    }
}
