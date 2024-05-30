package dev.jacktym.coflflip.macros;

import dev.jacktym.coflflip.Main;
import dev.jacktym.coflflip.config.FlipConfig;
import dev.jacktym.coflflip.util.*;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AutoClaimSold {
    @SubscribeEvent
    public void clientChatReceivedEvent(ClientChatReceivedEvent event) {
        if (!FlipConfig.autoClaimSold) {
            return;
        }
        String message = event.message.getUnformattedText();

        if (message.startsWith("§6[Auction]") && message.contains("bought")) {
            QueueUtil.addToQueue(() -> {
                RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> claimAuction((GuiScreenEvent) guiScreenEvent, message.split("[Auction] ")[1].split(" bought")[0]), "AutoClaimSold");
                Main.mc.thePlayer.sendChatMessage(event.message.getChatStyle().getChatClickEvent().getValue());
                long closeTime = System.currentTimeMillis() + Long.parseLong(FlipConfig.autoCloseMenuDelay);
                RealtimeEventRegistry.registerEvent("clientTickEvent", clientTickEvent -> Failsafes.closeGuiFailsafe((TickEvent.ClientTickEvent) clientTickEvent, closeTime, "AutoClaimSold"), "AutoClaimSold");
            });
        }
    }

    public static boolean claimAuction(GuiScreenEvent event, String buyer) {
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

                    if (FlipConfig.soldWebhooks) {
                        FlipItem item = FlipItem.getItemByUuid(FlipItem.getUuid(soldItem));
                        if (item != null) {
                            item.buyer = buyer;
                            DiscordIntegration.sendToWebsocket("FlipSold", item.serialize().toString());
                        }
                    }
                });
                return true;
            }
        }
        return false;
    }
}
