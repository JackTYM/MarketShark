package dev.jacktym.coflflip.macros;

import dev.jacktym.coflflip.Main;
import dev.jacktym.coflflip.config.FlipConfig;
import dev.jacktym.coflflip.util.RealtimeEventRegistry;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;

public class AutoBuy {
    public static Thread buyThread;
    public static void autoBuy() {
        if (buyThread == null) {
            buyThread = new Thread(() -> {
                RealtimeEventRegistry.registerEvent("guiOpenEvent", guiOpenEvent -> startBuy((GuiOpenEvent) guiOpenEvent));
            });
            buyThread.start();
        } else {
            System.out.println("Stopped AutoBuy. Thread running!");
        }
    }

    public static boolean startBuy(GuiOpenEvent event) {
        if (Main.mc.currentScreen instanceof GuiChest) {
            IInventory chest = ((ContainerChest) ((GuiChest) Main.mc.currentScreen).inventorySlots).getLowerChestInventory();

            if (chest.getDisplayName().toString().contains("BIN Auction View")) {
                ItemStack buyItem = chest.getStackInSlot(31);

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
                    Main.mc.playerController.windowClick(Main.mc.thePlayer.openContainer.windowId, 31, 0, 0, Main.mc.thePlayer);
                    RealtimeEventRegistry.registerEvent("guiOpenEvent", guiOpenEvent -> confirmPurchase((GuiOpenEvent) guiOpenEvent));
                }
            }
        }
        return false;
    }

    public static boolean confirmPurchase(GuiOpenEvent event) {
        if (Main.mc.currentScreen instanceof GuiChest) {
            IInventory chest = ((ContainerChest) ((GuiChest) Main.mc.currentScreen).inventorySlots).getLowerChestInventory();

            if (chest.getDisplayName().toString().contains("Confirm Purchase")) {
                Main.mc.playerController.windowClick(Main.mc.thePlayer.openContainer.windowId, 11, 0, 0, Main.mc.thePlayer);
            }
        }
        return false;
    }
}
