package dev.jacktym.coflflip.macros;

import dev.jacktym.coflflip.Main;
import dev.jacktym.coflflip.config.FlipConfig;
import dev.jacktym.coflflip.util.RealtimeEventRegistry;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;

public class AutoClaim {
    public static void claim(ItemStack item) {
        if (!FlipConfig.autoClaim) {
            return;
        }
        Main.mc.thePlayer.sendChatMessage("/ah");
        RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> openBids((GuiScreenEvent) guiScreenEvent, item));
    }

    public static boolean openBids(GuiScreenEvent event, ItemStack item) {
        if (Main.mc.currentScreen instanceof GuiChest) {
            IInventory chest = ((ContainerChest) ((GuiChest) Main.mc.currentScreen).inventorySlots).getLowerChestInventory();

            if (chest.getDisplayName().toString().equals("Co-op Auction House")) {
                Main.mc.playerController.windowClick(Main.mc.thePlayer.openContainer.windowId, 13, 0, 0, Main.mc.thePlayer);
                RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> claimItem((GuiScreenEvent) guiScreenEvent, item));
                return true;
            }
        }
        return false;
    }

    public static boolean claimItem(GuiScreenEvent event, ItemStack item) {
        if (Main.mc.currentScreen instanceof GuiChest) {
            IInventory chest = ((ContainerChest) ((GuiChest) Main.mc.currentScreen).inventorySlots).getLowerChestInventory();

            if (chest.getDisplayName().toString().equals("Your Bids")) {
                for (int i = 10; i <= 16; i++) {
                    if (chest.getStackInSlot(i).getDisplayName().equals(item.getDisplayName())) {
                        Main.mc.playerController.windowClick(Main.mc.thePlayer.openContainer.windowId, i, 0, 0, Main.mc.thePlayer);
                        RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> confirmClaim((GuiScreenEvent) guiScreenEvent, item));
                        return true;
                    }
                }

                return true;
            }
        }

        return false;
    }

    public static boolean confirmClaim(GuiScreenEvent event, ItemStack item) {
        if (Main.mc.currentScreen instanceof GuiChest) {
            IInventory chest = ((ContainerChest) ((GuiChest) Main.mc.currentScreen).inventorySlots).getLowerChestInventory();

            if (chest.getDisplayName().toString().equals("BIN Auction View")) {
                Main.mc.playerController.windowClick(Main.mc.thePlayer.openContainer.windowId, 31, 0, 0, Main.mc.thePlayer);
                RealtimeEventRegistry.registerEvent("clientChatReceivedEvent", clientChatReceivedEvent -> waitForClaimMessage((ClientChatReceivedEvent) clientChatReceivedEvent, System.currentTimeMillis() + 10000, item));
                return true;
            }
        }
        return false;
    }

    public static boolean waitForClaimMessage(ClientChatReceivedEvent event, Long expiryTime, ItemStack item) {
        if (expiryTime > System.currentTimeMillis()) {
            return true;
        }

        String message = event.message.toString();
        if (message.startsWith("You claimed") && message.contains(item.getDisplayName())) {
            AutoList.listItem(item, true);
            return true;
        }

        return false;
    }
}
