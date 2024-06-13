package dev.jacktym.marketshark.macros;

import dev.jacktym.marketshark.Main;
import dev.jacktym.marketshark.config.FlipConfig;
import dev.jacktym.marketshark.util.FlipItem;
import dev.jacktym.marketshark.util.QueueUtil;

public class AutoOpen {
    public static void openAuction(FlipItem item) {
        if (!FlipConfig.autoOpen) {
            return;
        }
        QueueUtil.addToQueue(() -> {
            if (FlipConfig.debug) {
                System.out.println("Attempting Open: " + item.auctionId);
            }
            if (Main.mc.thePlayer != null) {
                AutoBuy.autoBuy(item);
                Main.mc.thePlayer.sendChatMessage("/viewauction " + item.auctionId);
            }
            QueueUtil.finishAction("AutoOpen");
        });
    }
}
