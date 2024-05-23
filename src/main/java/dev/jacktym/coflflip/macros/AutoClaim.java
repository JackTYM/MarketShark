package dev.jacktym.coflflip.macros;

import dev.jacktym.coflflip.Main;
import dev.jacktym.coflflip.config.FlipConfig;
import dev.jacktym.coflflip.util.*;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;

public class AutoClaim {
    public static void claim(ItemStack item) {
        if (!FlipConfig.autoClaim) {
            return;
        }

        QueueUtil.addToQueue(() -> {
            RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> openBids((GuiScreenEvent) guiScreenEvent, item), "AutoClaim");
            Main.mc.thePlayer.sendChatMessage("/ah");
        });
    }

    public static boolean openBids(GuiScreenEvent event, ItemStack item) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiChest) {
            IInventory chest = GuiUtil.getInventory(event.gui);
            if (chest.getStackInSlot(0) == null) {
                return false;
            }

            System.out.println("\"" + chest.getDisplayName().getUnformattedText() + "\"");
            if (chest.getDisplayName().getUnformattedText().equals("Co-op Auction House")) {
                System.out.println("Clicking");
                DelayUtils.delayAction(300, () -> {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> claimItem((GuiScreenEvent) guiScreenEvent, item), "AutoClaim");
                    GuiUtil.tryClick(13);
                });
                return true;
            }
        }
        return false;
    }

    public static boolean claimItem(GuiScreenEvent event, ItemStack item) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiChest) {
            IInventory chest = GuiUtil.getInventory(event.gui);
            if (chest.getStackInSlot(0) == null) {
                return false;
            }

            if (chest.getDisplayName().getUnformattedText().equals("Your Bids")) {
                for (int i = 10; i <= 16; i++) {
                    if (chest.getStackInSlot(i) == null) {
                        return false;
                    }
                    if (chest.getStackInSlot(i).getDisplayName().equals(item.getDisplayName())) {
                        int finalI = i;
                        DelayUtils.delayAction(300, () -> {
                            RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> confirmClaim((GuiScreenEvent) guiScreenEvent, item), "AutoClaim");
                            GuiUtil.tryClick(finalI);
                        });
                        return true;
                    }
                }

                return true;
            }
        }

        return false;
    }

    public static boolean confirmClaim(GuiScreenEvent event, ItemStack item) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiChest) {
            IInventory chest = GuiUtil.getInventory(event.gui);
            if (chest.getStackInSlot(0) == null) {
                return false;
            }

            if (chest.getDisplayName().getUnformattedText().equals("BIN Auction View")) {
                DelayUtils.delayAction(300, () -> {
                    System.out.println("Claiming");
                    long expiryTime = System.currentTimeMillis() + 10000;
                    RealtimeEventRegistry.registerEvent("clientChatReceivedEvent", clientChatReceivedEvent -> waitForClaimMessage((ClientChatReceivedEvent) clientChatReceivedEvent, expiryTime, item), "AutoClaim");
                    GuiUtil.tryClick(31);
                });
                return true;
            }
        }
        return false;
    }

    public static boolean waitForClaimMessage(ClientChatReceivedEvent event, Long expiryTime, ItemStack item) {
        System.out.println(expiryTime);
        System.out.println(System.currentTimeMillis());
        if (expiryTime < System.currentTimeMillis()) {
            QueueUtil.finishAction();
            RealtimeEventRegistry.clearClazzMap("AutoClaim");
            return true;
        }

        String message = event.message.getUnformattedText();
        System.out.println(message);
        System.out.println(ChatUtils.stripColor(item.getDisplayName()));
        if (message.startsWith("You claimed") && message.contains(ChatUtils.stripColor(item.getDisplayName()))) {
            AutoList.listItem(item, true);
            QueueUtil.finishAction();
            RealtimeEventRegistry.clearClazzMap("AutoClaim");
            return true;
        }

        return false;
    }
}
