package dev.jacktym.coflflip.macros;

import dev.jacktym.coflflip.Main;
import dev.jacktym.coflflip.config.FlipConfig;
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
        RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> startBuy((GuiScreenEvent) guiScreenEvent));
    }

    public static boolean startBuy(GuiScreenEvent event) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiChest) {
            IInventory chest = ((ContainerChest) ((GuiChest) event.gui).inventorySlots).getLowerChestInventory();
            ItemStack item = chest.getStackInSlot(13);

            if (chest.getDisplayName().toString().contains("BIN Auction View")) {
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
                    return true;
                } else if (buyItem.getItem().equals(Items.gold_nugget)) {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> confirmPurchase((GuiScreenEvent) guiScreenEvent, item));
                    Main.mc.playerController.windowClick(Main.mc.thePlayer.openContainer.windowId, 31, 0, 0, Main.mc.thePlayer);
                }
            } else if (chest.getDisplayName().toString().contains("BIN Auction View")) {
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

            if (chest.getDisplayName().toString().contains("Confirm Purchase")) {
                if (chest.getStackInSlot(11) == null) {
                    return false;
                }
                RealtimeEventRegistry.registerEvent("clientChatReceivedEvent", clientChatReceivedEvent -> waitForBuyMessage((ClientChatReceivedEvent) clientChatReceivedEvent, System.currentTimeMillis() + 10000, item));

                Main.mc.playerController.windowClick(Main.mc.thePlayer.openContainer.windowId, 11, 0, 0, Main.mc.thePlayer);
                return true;
            }
        }
        return false;
    }

    public static boolean waitForBuyMessage(ClientChatReceivedEvent event, Long expiryTime, ItemStack item) {
        if (expiryTime > System.currentTimeMillis()) {
            return true;
        }

        System.out.println(item.getDisplayName());
        String message = event.message.toString();
        System.out.println(message);
        if (message.startsWith("You Purchased") && message.contains(item.getDisplayName())) {
            AutoClaim.claim(item);
            return true;
        }

        return false;
    }
}
