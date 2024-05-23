package dev.jacktym.coflflip.macros;

import dev.jacktym.coflflip.Main;
import dev.jacktym.coflflip.config.FlipConfig;
import dev.jacktym.coflflip.util.QueueUtil;
import net.minecraft.client.gui.inventory.GuiChest;

public class AutoOpen {
    public static void openAuction(String auction) {
        if (!FlipConfig.autoOpen) {
            return;
        }
        QueueUtil.addToQueue(() -> {
            if (Main.mc.thePlayer != null && !(Main.mc.currentScreen instanceof GuiChest)) {
                AutoBuy.autoBuy();
                Main.mc.thePlayer.sendChatMessage(auction);
                QueueUtil.finishAction();
            }
        });
    }
}
