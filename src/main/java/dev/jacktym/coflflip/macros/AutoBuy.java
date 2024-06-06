package dev.jacktym.coflflip.macros;

import dev.jacktym.coflflip.Main;
import dev.jacktym.coflflip.config.FlipConfig;
import dev.jacktym.coflflip.util.*;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;

public class AutoBuy {
    public static FlipItem item = null;
    private static int buyWindowId = 0;
    private static int confirmWindowId = 0;

    public static void autoBuy(FlipItem item) {
        if (!FlipConfig.autoBuy) {
            return;
        }

        QueueUtil.addToQueue(() -> {
            AutoBuy.item = item;
            RealtimeEventRegistry.registerPacket(AutoBuy::receivePacket, "AutoBuy");
            RealtimeEventRegistry.registerEvent("clientChatReceivedEvent", clientChatReceivedEvent -> notEnoughCoinsFailsafe((ClientChatReceivedEvent) clientChatReceivedEvent), "AutoBuy");
            RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> Failsafes.closeGuiFailsafe((GuiScreenEvent) guiScreenEvent, "AutoBuy"), "AutoBuy");
        });
    }

    public static boolean receivePacket(Packet packet) {
        if (packet instanceof S2DPacketOpenWindow) {
            S2DPacketOpenWindow p = (S2DPacketOpenWindow) packet;

            if (p.getWindowTitle().getUnformattedText().equals("BIN Auction View")) {
                AutoBuy.item.startTime = System.currentTimeMillis();
                buyWindowId = p.getWindowId();
            } else if (p.getWindowTitle().getUnformattedText().equals("Auction View")) {
                if (FlipConfig.debug) {
                    ChatUtils.printMarkedChat("BID Auction! Leaving Menu");
                }
                FlipItem.flipItems.remove(AutoBuy.item);
                FlipItem.flipMap.remove(AutoBuy.item.uuid);
                Main.mc.thePlayer.closeScreen();
                Main.mc.thePlayer.closeScreenAndDropStack();
                RealtimeEventRegistry.clearClazzMap("AutoBuy");
                QueueUtil.finishAction();
                return true;
            } else if (p.getWindowTitle().getUnformattedText().equals("Confirm Purchase")) {
                confirmWindowId = p.getWindowId();
            }
        } else if (packet instanceof S2FPacketSetSlot) {
            S2FPacketSetSlot p = (S2FPacketSetSlot) packet;

            if (p.func_149175_c() == buyWindowId) {
                if (p.func_149173_d() == 13) {
                    AutoBuy.item.setItemStack(p.func_149174_e());
                } else if (p.func_149173_d() == 31) {
                    System.out.println(p.func_149174_e().getItem().getUnlocalizedName());
                    System.out.println(p.func_149174_e().getItem().equals(Items.gold_nugget));
                    System.out.println(p.func_149174_e().getItem().equals(Items.potato));
                    System.out.println("\"" + ChatUtils.stripColor(p.func_149174_e().getDisplayName()) + "\"");
                    if (p.func_149174_e().getItem().equals(Items.potato) || ChatUtils.stripColor(p.func_149174_e().getDisplayName()).equals("Collect Auction")) {
                        if (FlipConfig.debug) {
                            ChatUtils.printMarkedChat("Lost Flip! Leaving Menu");
                        }
                        FlipItem.flipItems.remove(AutoBuy.item);
                        FlipItem.flipMap.remove(AutoBuy.item.uuid);
                        Main.mc.thePlayer.closeScreen();
                        RealtimeEventRegistry.clearClazzMap("AutoBuy");
                        QueueUtil.finishAction();
                        return true;
                    } else if (p.func_149174_e().getItem().equals(Items.bed)) {
                        // Buy bed Here
                        item.bed = true;
                        new Thread(() -> {
                            try {
                                long bedTime = item.auctionStart + 20000;

                                Thread.sleep(bedTime - System.currentTimeMillis());
                                System.out.println("Spamming in " + (bedTime - System.currentTimeMillis()) / 1000);

                                for (int i = 0; i < Integer.parseInt(FlipConfig.bedBuyRepeats); i++) {
                                    Main.mc.thePlayer.sendQueue.addToSendQueue(
                                            new C0EPacketClickWindow(buyWindowId, 31, 2, 3,
                                                    p.func_149174_e(),
                                                    Main.mc.thePlayer.openContainer.getNextTransactionID(Main.mc.thePlayer.inventory)));
                                    Thread.sleep(Integer.parseInt(FlipConfig.bedSpamDelay));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                ChatUtils.printMarkedChat("Exception during bed spam. Report this!");
                                FlipItem.flipItems.remove(AutoBuy.item);
                                FlipItem.flipMap.remove(AutoBuy.item.uuid);
                                Main.mc.thePlayer.closeScreen();
                                Main.mc.thePlayer.closeScreenAndDropStack();
                                RealtimeEventRegistry.clearClazzMap("AutoBuy");
                                QueueUtil.finishAction();
                            }
                        }).start();
                    } else if (p.func_149174_e().getItem().equals(Items.gold_nugget)) {
                        Main.mc.thePlayer.sendQueue.addToSendQueue(
                                new C0EPacketClickWindow(buyWindowId, 31, 2, 3,
                                        p.func_149174_e(),
                                        Main.mc.thePlayer.openContainer.getNextTransactionID(Main.mc.thePlayer.inventory)));

                        System.out.println("Attempted Click");
                    } else if (p.func_149174_e().getItem().equals(Item.getItemFromBlock(Blocks.barrier))) {
                        if (FlipConfig.debug) {
                            ChatUtils.printMarkedChat("Auction Cancelled! Leaving Menu");
                        }
                        FlipItem.flipItems.remove(AutoBuy.item);
                        FlipItem.flipMap.remove(AutoBuy.item.uuid);
                        Main.mc.thePlayer.closeScreen();
                        Main.mc.thePlayer.closeScreenAndDropStack();
                        RealtimeEventRegistry.clearClazzMap("AutoBuy");
                        QueueUtil.finishAction();
                        return true;
                    } else if (ChatUtils.stripColor(p.func_149174_e().getDisplayName()).equals("Loading...")) {
                        System.out.println("Flip Loading. Waiting!");
                    } else {
                        if (FlipConfig.debug) {
                            ChatUtils.printMarkedChat("Unknown Buy Item! May be users own auction! Leaving Menu | " + p.func_149174_e().getDisplayName());
                        }
                        FlipItem.flipItems.remove(AutoBuy.item);
                        FlipItem.flipMap.remove(AutoBuy.item.uuid);
                        Main.mc.thePlayer.closeScreen();
                        Main.mc.thePlayer.closeScreenAndDropStack();
                        RealtimeEventRegistry.clearClazzMap("AutoBuy");
                        QueueUtil.finishAction();
                        return true;
                    }
                }
            } else if (p.func_149175_c() == confirmWindowId) {
                if (p.func_149173_d() == 11) {
                    if (p.func_149174_e().getItem().equals(Item.getItemFromBlock(Blocks.stained_hardened_clay))) {
                        Main.mc.thePlayer.sendQueue.addToSendQueue(
                                new C0EPacketClickWindow(confirmWindowId, 11, 2, 3,
                                        p.func_149174_e(),
                                        Main.mc.thePlayer.openContainer.getNextTransactionID(Main.mc.thePlayer.inventory)));

                        AutoBuy.item.buyPrice = Long.parseLong(ChatUtils.stripColor(p.func_149174_e().getTagCompound().getCompoundTag("display").getTagList("Lore", 8).getStringTagAt(1).split("Cost: ")[1].split(" ")[0].replace(",", "")));
                        long expiryTime = System.currentTimeMillis() + 10000;
                        RealtimeEventRegistry.registerEvent("clientChatReceivedEvent", clientChatReceivedEvent -> AutoBuy.waitForBuyMessage((ClientChatReceivedEvent) clientChatReceivedEvent, expiryTime, AutoBuy.item), "AutoBuy");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean waitForBuyMessage(ClientChatReceivedEvent event, Long expiryTime, FlipItem item) {
        if (expiryTime < System.currentTimeMillis()) {
            Main.mc.thePlayer.closeScreen();
            QueueUtil.finishAction();
            RealtimeEventRegistry.clearClazzMap("AutoBuy");
            return true;
        }

        String message = ChatUtils.stripColor(event.message.getUnformattedText());
        if (message.startsWith("Putting coins in escrow")) {
            item.buyTime = System.currentTimeMillis();
            item.buySpeed = (int) (item.buyTime - item.startTime);

            Main.mc.thePlayer.closeScreen();
            Main.mc.thePlayer.closeScreenAndDropStack();
            return false;
        } else if (message.startsWith("You purchased") && message.contains(item.strippedDisplayName)) {
            ChatUtils.printMarkedChat("Purchased " + EnumChatFormatting.LIGHT_PURPLE + item.strippedDisplayName + EnumChatFormatting.RESET + " for " + EnumChatFormatting.GOLD + ChatUtils.abbreviateNumber(item.buyPrice) + EnumChatFormatting.RESET + " coins in " + EnumChatFormatting.GREEN + item.buySpeed + "ms" + EnumChatFormatting.RESET + " worth " + EnumChatFormatting.GOLD + ChatUtils.abbreviateNumber(item.coflWorth) + EnumChatFormatting.RESET + " coins for a " + EnumChatFormatting.GOLD + ChatUtils.abbreviateNumber(item.coflWorth - item.buyPrice) + EnumChatFormatting.RESET + " coin profit!");
            AutoClaim.claim(item);
            RealtimeEventRegistry.clearClazzMap("AutoBuy");
            Main.mc.thePlayer.closeScreen();
            QueueUtil.finishAction();
            item.bought = true;

            DiscordIntegration.sendToWebsocket("FlipBought", item.serialize().toString());
            return true;
        }
        return false;
    }

    public static boolean notEnoughCoinsFailsafe(ClientChatReceivedEvent event) {
        if (event.message.getUnformattedText().contains("You don't have enough coins to afford this bid!") || event.message.getUnformattedText().contains("This auction wasn't found!")) {
            if (!QueueUtil.currentAction.isEmpty()) {
                FlipItem.flipItems.remove(AutoBuy.item);
                FlipItem.flipMap.remove(AutoBuy.item.uuid);
                Main.mc.thePlayer.closeScreen();
                Main.mc.thePlayer.closeScreenAndDropStack();
                QueueUtil.finishAction();
                RealtimeEventRegistry.clearClazzMap("AutoBuy");
            }
            return true;
        }
        return false;
    }
}