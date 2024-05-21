package dev.jacktym.coflflip.macros;

import dev.jacktym.coflflip.Main;
import dev.jacktym.coflflip.config.FlipConfig;
import dev.jacktym.coflflip.util.RealtimeEventRegistry;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;

public class AutoList {
    public static void listItem(ItemStack item, boolean coflValue) {
        if (!FlipConfig.autoSell) {
            return;
        }

        Main.mc.thePlayer.sendChatMessage("/ah");
        RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> openManageAuctions((GuiScreenEvent) guiScreenEvent, item));
    }

    public static boolean openManageAuctions(GuiScreenEvent event, ItemStack item) {
        if (Main.mc.currentScreen instanceof GuiChest) {
            IInventory chest = ((ContainerChest) ((GuiChest) Main.mc.currentScreen).inventorySlots).getLowerChestInventory();

            if (chest.getDisplayName().getUnformattedText().equals("Co-op Auction House")) {
                Main.mc.playerController.windowClick(Main.mc.thePlayer.openContainer.windowId, 15, 0, 0, Main.mc.thePlayer);
                RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> openCreateAuction((GuiScreenEvent) guiScreenEvent, item));
                return true;
            }
        }
        return false;
    }

    public static boolean openCreateAuction(GuiScreenEvent event, ItemStack item) {
        if (Main.mc.currentScreen instanceof GuiChest) {
            IInventory chest = ((ContainerChest) ((GuiChest) Main.mc.currentScreen).inventorySlots).getLowerChestInventory();

            if (chest.getDisplayName().getUnformattedText().equals("Create Auction")) {
                Main.mc.playerController.windowClick(Main.mc.thePlayer.openContainer.windowId, 48, 0, 0, Main.mc.thePlayer);
                RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> openCreateAuction((GuiScreenEvent) guiScreenEvent, item));
                return false;
            } else if (chest.getDisplayName().getUnformattedText().equals("Create BIN Auction")) {
                System.out.println("Inventory Length: " + Main.mc.thePlayer.getInventory().length);
                return true;
            }
        }
        return false;
    }
}
