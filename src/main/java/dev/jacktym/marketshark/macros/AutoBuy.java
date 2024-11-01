package dev.jacktym.marketshark.macros;

import dev.jacktym.marketshark.Main;
import dev.jacktym.marketshark.config.FlipConfig;
import dev.jacktym.marketshark.util.*;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.TimerTask;

public class AutoBuy {
    public static FlipItem item = null;
    private static int buyWindowId = 0;
    private static int confirmWindowId = 0;

    public static void autoBuy(FlipItem item) {
        if (!FlipConfig.autoBuy || Main.paused || !DiscordIntegration.activated) {
            return;
        }

        QueueUtil.addToStartOfQueue(() -> {
            AutoBuy.item = item;
            RealtimeEventRegistry.registerPacket(AutoBuy::receivePacket, "AutoBuy");
            RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> closeBuy((GuiScreenEvent) guiScreenEvent), "AutoBuy");
            RealtimeEventRegistry.registerEvent("clientChatReceivedEvent", clientChatReceivedEvent -> notEnoughCoinsFailsafe((ClientChatReceivedEvent) clientChatReceivedEvent), "AutoBuy");
            RealtimeEventRegistry.registerEvent("clientTickEvent", clientTickEvent -> Failsafes.stuckEventFailsafe((TickEvent.ClientTickEvent) clientTickEvent, System.currentTimeMillis(), "AutoBuy"), "AutoBuy");
        });
    }

    private static TimerTask closeGuiTimer;

    public static void confirmClosed() {
        item.closed = true;
        if (Main.mc.currentScreen != null) {
            Main.mc.thePlayer.closeScreen();
        }
        System.out.println("Closed GUI");
        try {
            if (closeGuiTimer != null) {
                closeGuiTimer.cancel();
                closeGuiTimer = null;
            }
        } catch (Exception e) {
            BugLogger.logError(e);
        }
        closeGuiTimer = DelayUtils.delayAction(500, () -> {
            if (Main.mc.currentScreen == null) {
                RealtimeEventRegistry.clearClazzMap("AutoBuy");
            } else {
                confirmClosed();
            }
        });
    }

    // Packet-based approach occasionally fails
    public static boolean closeBuy(GuiScreenEvent event) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiChest) {
            IInventory chest = GuiUtil.getInventory(event.gui);

            if (chest.getDisplayName().getUnformattedText().equals("BIN Auction View")) {
                ItemStack buyItem = chest.getStackInSlot(31);
                if (buyItem == null) {
                    return false;
                }
                if (buyItem.getItem().equals(Items.poisonous_potato)) {
                    BugLogger.logChat("Not enough coins for flip! Leaving Menu", FlipConfig.debug);
                    FlipItem.flipItems.remove(AutoBuy.item);
                    FlipItem.flipMap.remove(AutoBuy.item.uuid);
                    confirmClosed();
                } else if (buyItem.getItem().equals(Items.potato) || ChatUtils.stripColor(buyItem.getDisplayName()).equals("Collect Auction")) {
                    BugLogger.log("Lost Flip Potato! " + buyItem.getDisplayName(), FlipConfig.debug);
                    BugLogger.logChat("Lost Flip! Leaving Menu", true);
                    FlipItem.flipItems.remove(AutoBuy.item);
                    FlipItem.flipMap.remove(AutoBuy.item.uuid);

                    DiscordIntegration.sendToWebsocket("FlipLost", item.serialize().toString());

                    confirmClosed();
                } else if ((buyItem.getItem().equals(Items.gold_nugget)
                        || (ChatUtils.stripColor(buyItem.getDisplayName()).equals("Buy Item Right Now") && !buyItem.getItem().equals(Items.bed)))) {
                    //#if >=Megalodon
                    if (!item.skipped) {
                        return false;
                    }
                    //#endif >=Megalodon
                    Main.mc.thePlayer.sendQueue.addToSendQueue(
                            new C0EPacketClickWindow(Main.mc.thePlayer.openContainer.windowId, 31, 2, 3,
                                    buyItem,
                                    Main.mc.thePlayer.openContainer.getNextTransactionID(Main.mc.thePlayer.inventory)));

                    System.out.println("Attempted Backup Click On Item: " + buyItem.getItem().getUnlocalizedName() + " With name " + ChatUtils.stripColor(buyItem.getDisplayName()));
                } else if (buyItem.getItem().equals(Item.getItemFromBlock(Blocks.barrier))) {
                    BugLogger.logChat("Auction Cancelled! Leaving Menu", FlipConfig.debug);
                    FlipItem.flipItems.remove(AutoBuy.item);
                    FlipItem.flipMap.remove(AutoBuy.item.uuid);
                    confirmClosed();
                } else if (!buyItem.getItem().equals(Items.bed) && !ChatUtils.stripColor(buyItem.getDisplayName()).contains("Loading")) {
                    BugLogger.logChat("Unknown Buy Item! May be users own auction! Leaving Menu | " + buyItem.getDisplayName() + " " + buyItem.getItem().getUnlocalizedName(), FlipConfig.debug);
                    FlipItem.flipItems.remove(AutoBuy.item);
                    FlipItem.flipMap.remove(AutoBuy.item.uuid);
                    confirmClosed();
                }
            } else if (chest.getDisplayName().getUnformattedText().equals("Auction View")) {
                BugLogger.logChat("BID Auction! Leaving Menu", FlipConfig.debug);
                FlipItem.flipItems.remove(AutoBuy.item);
                FlipItem.flipMap.remove(AutoBuy.item.uuid);
                confirmClosed();
            } else if (chest.getDisplayName().getUnformattedText().equals("Confirm Purchase")) {
                ItemStack confirmItem = chest.getStackInSlot(11);
                if (confirmItem == null) {
                    return false;
                }

                if (confirmItem.getItem().equals(Item.getItemFromBlock(Blocks.stained_hardened_clay))) {
                    if (item.buyClicks < FlipConfig.maxBuyClicks) {
                        item.buyClicks++;
                        Main.mc.thePlayer.sendQueue.addToSendQueue(
                                new C0EPacketClickWindow(Main.mc.thePlayer.openContainer.windowId, 11, 2, 3,
                                        confirmItem,
                                        Main.mc.thePlayer.openContainer.getNextTransactionID(Main.mc.thePlayer.inventory)));

                        AutoBuy.item.buyPrice = Long.parseLong(ChatUtils.stripColor(confirmItem.getTagCompound().getCompoundTag("display").getTagList("Lore", 8).getStringTagAt(1).split("Cost: ")[1].split(" ")[0].replace(",", "")));
                        long expiryTime = System.currentTimeMillis() + 10000;
                        RealtimeEventRegistry.registerEvent("clientChatReceivedEvent", clientChatReceivedEvent -> AutoBuy.waitForBuyMessage((ClientChatReceivedEvent) clientChatReceivedEvent, expiryTime, AutoBuy.item), "AutoBuy");
                        return false;
                    } else {
                        confirmClosed();
                    }
                }
            }
        }
        return false;
    }

    public static boolean receivePacket(Packet packet) {
        if (packet instanceof S2DPacketOpenWindow) {
            S2DPacketOpenWindow p = (S2DPacketOpenWindow) packet;

            if (p.getWindowTitle().getUnformattedText().equals("BIN Auction View")) {
                AutoBuy.item.startTime = System.currentTimeMillis();
                buyWindowId = p.getWindowId();
            } else if (p.getWindowTitle().getUnformattedText().equals("Confirm Purchase")) {
                confirmWindowId = p.getWindowId();
            }
        } else if (packet instanceof S2FPacketSetSlot) {
            S2FPacketSetSlot p = (S2FPacketSetSlot) packet;

            if (p.func_149175_c() == buyWindowId) {
                if (p.func_149173_d() == 13) {
                    AutoBuy.item.setItemStack(p.func_149174_e());

                    DiscordIntegration.sendToWebsocket("FlipLost", item.serialize().toString());
                } else if (p.func_149173_d() == 31) {
                    if (p.func_149174_e().getItem().equals(Items.bed)) {
                        // Buy bed Here
                        item.bed = true;
                        long bedTime = item.auctionStart + FlipConfig.bedSpamStartDelay;
                        long delay = Math.max(bedTime - System.currentTimeMillis(), 0);
                        System.out.println("Spamming item " + item.strippedDisplayName + " in " + delay + "ms");

                        item.bedClicking = true;
                        String itemName = item.strippedDisplayName;

                        DelayUtils.delayAction(delay, () -> clickBed(itemName, p));
                    } else if (p.func_149174_e().getItem().equals(Items.gold_nugget) || (ChatUtils.stripColor(p.func_149174_e().getDisplayName()).equals("Buy Item Right Now") && !p.func_149174_e().getItem().equals(Items.poisonous_potato))) {
                        Main.mc.thePlayer.sendQueue.addToSendQueue(
                                new C0EPacketClickWindow(buyWindowId, 31, 2, 3,
                                        p.func_149174_e(),
                                        Main.mc.thePlayer.openContainer.getNextTransactionID(Main.mc.thePlayer.inventory)));

                        System.out.println("Attempted Click");

                        //#if >=Megalodon
                        if (FlipConfig.confirmSkip) {
                            DelayUtils.delayAction(FlipConfig.confirmSkipDelay, () -> {
                                item.skipped = true;
                                ItemStack fakeConfirm = new ItemStack(Item.getItemFromBlock(Blocks.stained_hardened_clay), 1, 13);
                                fakeConfirm.setStackDisplayName("§aConfirm");

                                NBTTagCompound nbt = new NBTTagCompound();

                                nbt.setByte("overrideMeta", (byte) 1);
                                NBTTagCompound display = new NBTTagCompound();
                                NBTTagList lore = new NBTTagList();
                                lore.appendTag(new NBTTagString("§7Purchasing: " + AutoBuy.item.itemStack.getDisplayName()));
                                lore.appendTag(new NBTTagString("§7Cost: " + p.func_149174_e().getTagCompound().getCompoundTag("display").getTagList("Lore", 8).getStringTagAt(1).split("Price: ")[1]));
                                display.setTag("Lore", lore);
                                display.setString("Name", "§aConfirm");
                                nbt.setTag("display", display);

                                nbt.setTag("AttributeModifiers", new NBTTagList());
                                fakeConfirm.setTagCompound(nbt);

                                Main.mc.thePlayer.sendQueue.addToSendQueue(
                                        new C0EPacketClickWindow(buyWindowId + 1, 11, 2, 3,
                                                fakeConfirm,
                                                Main.mc.thePlayer.openContainer.getNextTransactionID(Main.mc.thePlayer.inventory)));

                                BugLogger.logChat("Attempted Confirm Skip with " + FlipConfig.confirmSkipDelay + "ms Delay!", true);

                                AutoBuy.item.buyPrice = Long.parseLong(ChatUtils.stripColor(p.func_149174_e().getTagCompound().getCompoundTag("display").getTagList("Lore", 8).getStringTagAt(1).split("Price: ")[1].split(" ")[0].replace(",", "")));
                                long expiryTime = System.currentTimeMillis() + 10000;
                                RealtimeEventRegistry.registerEvent("clientChatReceivedEvent", clientChatReceivedEvent -> AutoBuy.waitForBuyMessage((ClientChatReceivedEvent) clientChatReceivedEvent, expiryTime, AutoBuy.item), "AutoBuy");
                            });
                        }
                        //#endif >=Megalodon
                    }
                }
            } else if (p.func_149175_c() == confirmWindowId
                    && (
                    //#if >=Megalodon
                    !FlipConfig.confirmSkip ||
                            //#endif >=Megalodon
                            item.bed)
            ) {
                if (p.func_149173_d() == 11) {
                    System.out.println("Confirm Item Received: " + System.currentTimeMillis());
                    ItemStack confirmItem = p.func_149174_e();
                    if (confirmItem == null) {
                        return false;
                    }

                    System.out.println(confirmItem.getItem().getUnlocalizedName());
                    if (confirmItem.getItem().equals(Item.getItemFromBlock(Blocks.stained_hardened_clay))) {
                        if (item.buyClicks < FlipConfig.maxBuyClicks) {
                            item.buyClicks++;
                            Main.mc.thePlayer.sendQueue.addToSendQueue(
                                    new C0EPacketClickWindow(confirmWindowId, 11, 2, 3,
                                            p.func_149174_e(),
                                            Main.mc.thePlayer.openContainer.getNextTransactionID(Main.mc.thePlayer.inventory)));

                            AutoBuy.item.buyPrice = Long.parseLong(ChatUtils.stripColor(p.func_149174_e().getTagCompound().getCompoundTag("display").getTagList("Lore", 8).getStringTagAt(1).split("Cost: ")[1].split(" ")[0].replace(",", "")));
                            long expiryTime = System.currentTimeMillis() + 10000;
                            RealtimeEventRegistry.registerEvent("clientChatReceivedEvent", clientChatReceivedEvent -> AutoBuy.waitForBuyMessage((ClientChatReceivedEvent) clientChatReceivedEvent, expiryTime, AutoBuy.item), "AutoBuy");
                            return true;
                        } else {
                            confirmClosed();
                        }
                    }
                }
            }
        }
        return false;
    }

    public static void clickBed(String itemName, S2FPacketSetSlot p) {
        if (QueueUtil.currentAction.equals("AutoBuy") && item.bedClicking && !item.closed && itemName.equals(item.strippedDisplayName)) {
            DelayUtils.delayAction(FlipConfig.bedSpamDelay, () -> {
                Main.mc.thePlayer.sendQueue.addToSendQueue(
                        new C0EPacketClickWindow(buyWindowId, 31, 2, 3,
                                p.func_149174_e(),
                                Main.mc.thePlayer.openContainer.getNextTransactionID(Main.mc.thePlayer.inventory)));

                clickBed(itemName, p);
            });
        }
    }

    public static boolean waitForBuyMessage(ClientChatReceivedEvent event, Long expiryTime, FlipItem item) {
        if (expiryTime < System.currentTimeMillis()) {
            confirmClosed();
            return true;
        }

        String message = ChatUtils.stripColor(event.message.getUnformattedText());
        if (message.startsWith("Putting coins in escrow")) {
            item.buyTime = System.currentTimeMillis();
            item.buySpeed = (int) (item.buyTime - item.startTime);

            Main.mc.thePlayer.closeScreen();
            return false;
        } else if (message.startsWith("You purchased") && message.contains(item.strippedDisplayName)) {
            BugLogger.logChat("Purchased " + EnumChatFormatting.LIGHT_PURPLE + item.strippedDisplayName + EnumChatFormatting.RESET + " for " + EnumChatFormatting.GOLD + ChatUtils.abbreviateNumber(item.buyPrice) + EnumChatFormatting.RESET + " coins in " + EnumChatFormatting.GREEN + item.buySpeed + "ms" + EnumChatFormatting.RESET + " worth " + EnumChatFormatting.GOLD + ChatUtils.abbreviateNumber(item.coflWorth) + EnumChatFormatting.RESET + " coins for a " + EnumChatFormatting.GOLD + ChatUtils.abbreviateNumber(item.coflWorth - item.buyPrice) + EnumChatFormatting.RESET + " coin profit!", true);
            AutoClaim.claim(item);
            item.bought = true;
            RealtimeEventRegistry.clearClazzMap("AutoBuy");
            Main.mc.thePlayer.closeScreen();

            DiscordIntegration.sendToWebsocket("FlipBought", item.serialize().toString());
            return true;
        }
        return false;
    }

    public static boolean notEnoughCoinsFailsafe(ClientChatReceivedEvent event) {
        if (event.message.getUnformattedText().contains("You don't have enough coins to afford this bid!") || event.message.getUnformattedText().contains("This auction wasn't found!") || event.message.getUnformattedText().contains("You didn't participate in this auction!")) {
            if (!QueueUtil.currentAction.isEmpty()) {
                FlipItem.flipItems.remove(AutoBuy.item);
                FlipItem.flipMap.remove(AutoBuy.item.uuid);
                confirmClosed();
            }
            return true;
        }
        return false;
    }
}