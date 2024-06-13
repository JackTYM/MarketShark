package dev.jacktym.marketshark.util;

import dev.jacktym.marketshark.Main;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;

public class GuiUtil {
    public static void tryClick(int slotId) {
        Main.mc.playerController.windowClick(Main.mc.thePlayer.openContainer.windowId, slotId, 2, 3, Main.mc.thePlayer);
        Main.mc.playerController.windowClick(Main.mc.thePlayer.openContainer.windowId, slotId, 0, 0, Main.mc.thePlayer);
    }

    public static void singleClick(int slotId) {
        Main.mc.playerController.windowClick(Main.mc.thePlayer.openContainer.windowId, slotId, 2, 3, Main.mc.thePlayer);
    }

    public static void tryClickInventory(int slotId) {
        //Main.mc.playerController.windowClick(Main.mc.thePlayer.inventoryContainer.windowId, slotId, 2, 3, Main.mc.thePlayer);
        Main.mc.playerController.windowClick(Main.mc.thePlayer.inventoryContainer.windowId, slotId, 0, 0, Main.mc.thePlayer);
    }

    public static IInventory getInventory(GuiScreen gui) {
        return ((ContainerChest) ((GuiChest) gui).inventorySlots).getLowerChestInventory();
    }
}
