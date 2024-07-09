package dev.jacktym.marketshark.macros;

import dev.jacktym.marketshark.Main;
import dev.jacktym.marketshark.config.FlipConfig;
import dev.jacktym.marketshark.util.*;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;

import java.util.TimerTask;

public class AutoClaim {
    public static void claim(FlipItem item) {
        if (!FlipConfig.autoClaim || Main.paused || !DiscordIntegration.activated) {
            return;
        }

        QueueUtil.addToQueue(() -> {
            RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> openBids((GuiScreenEvent) guiScreenEvent, item), "AutoClaim");
            Main.mc.thePlayer.sendChatMessage("/ah");

            System.out.println("Opened AH to claim");
        });
    }

    public static boolean openBids(GuiScreenEvent event, FlipItem item) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiChest) {
            IInventory chest = GuiUtil.getInventory(event.gui);
            if (chest.getStackInSlot(13) == null) {
                return false;
            }

            if (chest.getDisplayName().getUnformattedText().contains("Auction House")) {
                DelayUtils.delayAction(300, () -> {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> claimItem((GuiScreenEvent) guiScreenEvent, item), "AutoClaim");
                    GuiUtil.singleClick(13);
                });
                return true;
            }
        }
        return false;
    }

    public static boolean claimItem(GuiScreenEvent event, FlipItem item) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiChest) {
            IInventory chest = GuiUtil.getInventory(event.gui);
            if (chest.getStackInSlot(10) == null) {
                return false;
            }

            if (chest.getDisplayName().getUnformattedText().equals("Your Bids")) {
                for (int i = 10; i <= 16; i++) {
                    if (chest.getStackInSlot(i) == null) {
                        return false;
                    }
                    if (chest.getStackInSlot(i).getDisplayName().equals(item.displayName)) {
                        int finalI = i;
                        DelayUtils.delayAction(500, () -> {
                            RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> confirmClaim((GuiScreenEvent) guiScreenEvent, item), "AutoClaim");
                            GuiUtil.singleClick(finalI);
                        });
                        return true;
                    }
                }

                return true;
            }
        }

        return false;
    }

    private static TimerTask claimTimer;

    public static boolean confirmClaim(GuiScreenEvent event, FlipItem item) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiChest) {
            IInventory chest = GuiUtil.getInventory(event.gui);
            if (chest.getStackInSlot(31) == null) {
                return false;
            }

            if (chest.getDisplayName().getUnformattedText().equals("BIN Auction View")) {
                if (claimTimer == null) {
                    claimTimer = DelayUtils.delayAction(500, () -> {
                        long expiryTime = System.currentTimeMillis() + 10000;
                        RealtimeEventRegistry.registerEvent("clientChatReceivedEvent", clientChatReceivedEvent -> waitForClaimMessage((ClientChatReceivedEvent) clientChatReceivedEvent, expiryTime, item), "AutoClaim");
                        GuiUtil.singleClick(31);
                        claimTimer = null;
                    });
                }
                return true;
            }
        }
        return false;
    }

    public static boolean waitForClaimMessage(ClientChatReceivedEvent event, Long expiryTime, FlipItem item) {
        if (expiryTime < System.currentTimeMillis()) {
            RealtimeEventRegistry.clearClazzMap("AutoClaim");
            return true;
        }

        String message = event.message.getUnformattedText();
        if (message.startsWith("You claimed") && message.contains(ChatUtils.stripColor(item.displayName))) {
            AutoList.listItem(item, true);
            
            RealtimeEventRegistry.clearClazzMap("AutoClaim");
            return true;
        }

        return false;
    }
}
