package dev.jacktym.coflflip.macros;

import dev.jacktym.coflflip.Main;
import dev.jacktym.coflflip.config.FlipConfig;
import dev.jacktym.coflflip.util.DelayUtils;
import dev.jacktym.coflflip.util.GuiUtil;
import dev.jacktym.coflflip.util.QueueUtil;
import dev.jacktym.coflflip.util.RealtimeEventRegistry;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoClaimSold {
    @SubscribeEvent
    public void clientChatReceivedEvent(ClientChatReceivedEvent event) {
        if (!FlipConfig.autoClaimSold) {
            return;
        }

        QueueUtil.addToQueue(() -> {
            String message = event.message.getUnformattedText();

            if (message.startsWith("§6[Auction]") && message.contains("bought")) {
                RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> claimAuction((GuiScreenEvent) guiScreenEvent), "AutoClaimSold");
                Main.mc.thePlayer.sendChatMessage(event.message.getChatStyle().getChatClickEvent().getValue());
                System.out.println(event.message.getChatStyle().getChatClickEvent().getValue());
            }
        });
    }

    public static boolean claimAuction(GuiScreenEvent event) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiChest) {
            IInventory chest = GuiUtil.getInventory(event.gui);
            if (chest.getStackInSlot(0) == null) {
                return false;
            }

            ItemStack soldItem = chest.getStackInSlot(13);
            if (soldItem == null) {
                return false;
            }

            if (chest.getDisplayName().getUnformattedText().equals("BIN Auction View")) {
                DelayUtils.delayAction(300, () -> {
                    GuiUtil.tryClick(31);
                    QueueUtil.finishAction();
                    RealtimeEventRegistry.clearClazzMap("AutoClaimSold");
                });
                return true;
            }
        }
        return false;
    }
}
