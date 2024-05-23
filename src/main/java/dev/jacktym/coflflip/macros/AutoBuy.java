package dev.jacktym.coflflip.macros;

import dev.jacktym.coflflip.Main;
import dev.jacktym.coflflip.config.FlipConfig;
import dev.jacktym.coflflip.util.ChatUtils;
import dev.jacktym.coflflip.util.GuiUtil;
import dev.jacktym.coflflip.util.QueueUtil;
import dev.jacktym.coflflip.util.RealtimeEventRegistry;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;

public class AutoBuy {
    public static void autoBuy() {
        if (!FlipConfig.autoBuy) {
            return;
        }

        QueueUtil.addToQueue(() -> {
            RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> startBuy((GuiScreenEvent) guiScreenEvent), "AutoBuy");
            long expiryTime = System.currentTimeMillis() + 20000;
            RealtimeEventRegistry.registerEvent("clientChatReceivedEvent", clientChatReceivedEvent -> notEnoughCoinsFailsafe((ClientChatReceivedEvent) clientChatReceivedEvent, expiryTime), "AutoBuy");
        });
    }

    public static boolean startBuy(GuiScreenEvent event) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiChest) {
            IInventory chest = GuiUtil.getInventory(event.gui);
            if (chest.getStackInSlot(0) == null) {
                return false;
            }
            ItemStack item = chest.getStackInSlot(13);

            if (chest.getDisplayName().getUnformattedText().equals("BIN Auction View")) {
                ItemStack buyItem = chest.getStackInSlot(31);
                if (buyItem == null) {
                    return false;
                }

                if (buyItem.getItem().equals(Items.potato)) {
                    if (FlipConfig.debug) {
                        System.out.println("Flip Purchased! Leaving Menu");
                    }
                    Main.mc.thePlayer.closeScreen();
                    return true;
                } else if (buyItem.getItem().equals(Items.bed)) {
                    // Buy bed Here
                    if (FlipConfig.debug) {
                        System.out.println("BED Auction! Leaving Menu");
                    }
                    Main.mc.thePlayer.closeScreen();
                    return true;
                } else if (buyItem.getItem().equals(Items.gold_nugget)) {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> confirmPurchase((GuiScreenEvent) guiScreenEvent, item), "AutoBuy");
                    GuiUtil.tryClick(31);
                } else {
                    if (FlipConfig.debug) {
                        System.out.println("Unknown Buy Item! May be users own auction!");
                    }
                    Main.mc.thePlayer.closeScreen();
                }
            } else if (chest.getDisplayName().getUnformattedText().equals("Auction View")) {
                if (FlipConfig.debug) {
                    System.out.println("BID Auction! Leaving Menu");
                }
                Main.mc.thePlayer.closeScreen();
                return true;
            }
        }
        return false;
    }

    public static boolean confirmPurchase(GuiScreenEvent event, ItemStack item) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }

        if (event.gui instanceof GuiChest) {
            IInventory chest = ((ContainerChest) ((GuiChest) event.gui).inventorySlots).getLowerChestInventory();
            if (chest.getStackInSlot(0) == null) {
                return false;
            }

            if (chest.getDisplayName().getUnformattedText().equals("Confirm Purchase")) {
                if (chest.getStackInSlot(11) == null) {
                    return false;
                }
                long expiryTime = System.currentTimeMillis() + 10000;
                RealtimeEventRegistry.registerEvent("clientChatReceivedEvent", clientChatReceivedEvent -> waitForBuyMessage((ClientChatReceivedEvent) clientChatReceivedEvent, expiryTime, item), "AutoBuy");

                GuiUtil.tryClick(11);
                return true;
            }
        }
        return false;
    }

    public static boolean waitForBuyMessage(ClientChatReceivedEvent event, Long expiryTime, ItemStack item) {
        if (expiryTime < System.currentTimeMillis()) {
            QueueUtil.finishAction();
            RealtimeEventRegistry.clearClazzMap("AutoBuy");
            return true;
        }

        String message = event.message.getUnformattedText();
        if (message.startsWith("Putting coins in escrow")) {
            Main.mc.thePlayer.closeScreen();
            QueueUtil.finishAction();
            RealtimeEventRegistry.clearClazzMap("AutoBuy");
            return false;
        } else if (message.contains(ChatUtils.stripColor(item.getDisplayName()))) {
            AutoClaim.claim(item);
            return true;
        }

        return false;
    }

    public static boolean notEnoughCoinsFailsafe(ClientChatReceivedEvent event, long expiryTime) {
        if (expiryTime < System.currentTimeMillis()) {
            return true;
        }

        if (event.message.getUnformattedText().contains("You don't have enough coins to afford this bid!")) {
            if (QueueUtil.doingAction) {
                Main.mc.thePlayer.closeScreen();
                QueueUtil.finishAction();
                RealtimeEventRegistry.clearClazzMap("AutoBuy");
            }
            return true;
        }
        return false;
    }
}