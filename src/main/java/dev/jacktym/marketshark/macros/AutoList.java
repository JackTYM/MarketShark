package dev.jacktym.marketshark.macros;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.jacktym.marketshark.Main;
import dev.jacktym.marketshark.config.FlipConfig;
import dev.jacktym.marketshark.mixins.GuiEditSignAccessor;
import dev.jacktym.marketshark.util.*;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.GuiScreenEvent;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AutoList {
    private static boolean currentlyListing = false;
    public static boolean listingInv = false;
    private static final Lock lock = new ReentrantLock();
    private static final Condition listingCondition = lock.newCondition();

    public static void listInventory() {
        if (currentlyListing || Main.paused || !DiscordIntegration.activated) {
            return;
        }

        BugLogger.logChat("Listing inventory! Use /ms reset to cancel", true);
        try {
            ItemStack[] inventory = Main.mc.thePlayer.inventory.mainInventory;
            JsonArray coflPrices = CoflAPIUtil.getCoflPrices(inventory);
            if (coflPrices != null) {
                for (int i = 0; i < inventory.length; i++) {
                    if (i == 8) {
                        // Skyblock Menu
                        continue;
                    }

                    int finalI = i;
                    QueueUtil.addToQueue(() -> listItem(FlipItem.getFlipItem(inventory[finalI]), true));
                }
            }
        } catch (Exception e) {
            BugLogger.logError(e);
        }
    }

    public static void finishCurrentListing() {
        lock.lock();
        try {
            currentlyListing = false;
            listingCondition.signal();
        } finally {
            lock.unlock();
        }
    }

    private static void waitForListingToFinish() throws InterruptedException {
        lock.lock();
        try {
            while (currentlyListing) {
                listingCondition.await();
            }
        } finally {
            lock.unlock();
        }
    }

    public static void listItem(FlipItem item, boolean automatic) {
        if (!FlipConfig.autoSell || item == null || currentlyListing) {
            return;
        }

        if (automatic) {
            if (!FlipConfig.flipper && item.finder.equals("FLIPPER")) {
                item.skipReason = "Skipped listing item. Flipper finder disabled!";
            } else if (!FlipConfig.sniper && item.finder.equals("SNIPER")) {
                item.skipReason = "Skipped listing item. Sniper finder disabled!";
            } else if (!FlipConfig.sniperMedian && item.finder.equals("SNIPER_MEDIAN")) {
                item.skipReason = "Skipped listing item. Sniper (Median) finder disabled!";
            } else if (!FlipConfig.user && item.finder.equals("USER")) {
                item.skipReason = "Skipped listing item. User finder disabled!";
            } else if (!FlipConfig.tfm && item.finder.equals("TFM")) {
                item.skipReason = "Skipped listing item. TFM finder disabled!";
            } else if (!FlipConfig.stonks && item.finder.equals("STONKS")) {
                item.skipReason = "Skipped listing item. Stonks finder disabled!";
            }

            if (!item.skipReason.isEmpty()) {
                BugLogger.logChat(item.skipReason, true);

                RealtimeEventRegistry.clearClazzMap("AutoList");
                finishCurrentListing();

                if (FlipConfig.listedWebhooks) {
                    DiscordIntegration.sendToWebsocket("ListingSkipped", item.serialize().toString());
                }
            }
        }

        if (FlipConfig.autoSellPrice == 4 && item.coflWorth != 0 && item.coflWorth != item.buyPrice) {
            item.sellPrice = item.coflWorth;
        } else {
            JsonObject coflPrice = CoflAPIUtil.getCoflPrice(item.itemStack);
            if (coflPrice != null) {
                switch (FlipConfig.autoSellPrice) {
                    case 0:
                        item.sellPrice = coflPrice.get("lbin").getAsLong();
                        break;
                    case 1:
                        item.sellPrice = (long) (coflPrice.get("lbin").getAsLong() * 0.95);
                        break;
                    case 4:
                        if (item.coflWorth == item.buyPrice) {
                            BugLogger.logChat("Buy Price = Worth. May be User Finder Flip. Defaulting to Median Price", true);
                        } else {
                            BugLogger.logChat("No Cofl Flip to use. Defaulting to Median Price", true);
                        }
                    case 2:
                        item.sellPrice = coflPrice.get("median").getAsLong();
                        break;
                    case 3:
                        item.sellPrice = (long) (coflPrice.get("median").getAsLong() * 0.95);
                        break;
                }
            } else {
                BugLogger.logChat("Failed to fetch Cofl API. Rate Limited?", true);
            }
        }

        float flipProfit = item.sellPrice - item.buyPrice - calculateBINTax(item.buyPrice);

        if (item.sellPrice < item.buyPrice) {
            BugLogger.logChat("Skipped listing item. Sell price less than buy price!", true);
            item.skipReason = "Skipped listing item. Sell price lower than buy price!";

            RealtimeEventRegistry.clearClazzMap("AutoList");
            finishCurrentListing();

            if (FlipConfig.listedWebhooks) {
                DiscordIntegration.sendToWebsocket("ListingSkipped", item.serialize().toString());
            }
            return;
        }

        if (FlipConfig.enableMaxList && item.sellPrice > FlipConfig.maximumAutoList) {
            BugLogger.logChat("Skipped listing item. Above maximum list!", true);
            item.skipReason = "Skipped listing item. Above maximum list!";

            RealtimeEventRegistry.clearClazzMap("AutoList");
            finishCurrentListing();

            if (FlipConfig.listedWebhooks) {
                DiscordIntegration.sendToWebsocket("ListingSkipped", item.serialize().toString());
            }
            return;
        }


        if (FlipConfig.enableMinProfitPercent && Math.round((flipProfit / item.sellPrice) * 100) < FlipConfig.minimumProfitPercent) {
            BugLogger.logChat("Skipped listing item. Below minimum profit percent!", true);
            item.skipReason = "Skipped listing item. Below minimum profit percent!";
            RealtimeEventRegistry.clearClazzMap("AutoList");
            finishCurrentListing();

            if (FlipConfig.listedWebhooks) {
                DiscordIntegration.sendToWebsocket("ListingSkipped", item.serialize().toString());
            }
            return;
        }

        BugLogger.logChat("Listing item " + item.strippedDisplayName + " for " + ChatUtils.abbreviateNumber(item.sellPrice) + " coins", true);

        QueueUtil.addToQueue(() -> {
            currentlyListing = true;
            Main.mc.thePlayer.sendChatMessage("/ah");
            RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> openManageAuctions((GuiScreenEvent) guiScreenEvent, item), "AutoList");
        });

        listItem(item);
    }

    public static void listItem(FlipItem item) {
        BugLogger.logChat("Listing item " + item.strippedDisplayName + " for " + ChatUtils.abbreviateNumber(item.sellPrice) + " coins", true);

        QueueUtil.addToQueue(() -> {
            currentlyListing = true;
            Main.mc.thePlayer.sendChatMessage("/ah");
            RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> openManageAuctions((GuiScreenEvent) guiScreenEvent, item), "AutoList");
        });
    }

    public static boolean openManageAuctions(GuiScreenEvent event, FlipItem item) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiChest) {
            IInventory chest = GuiUtil.getInventory(event.gui);
            if (chest.getStackInSlot(0) == null) {
                return false;
            }

            if (chest.getDisplayName().getUnformattedText().endsWith("Auction House")) {
                DelayUtils.delayAction(800, () -> {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> openCreateAuction((GuiScreenEvent) guiScreenEvent, item), "AutoList");
                    GuiUtil.singleClick(15);
                });
                return true;
            }
        }
        return false;
    }

    public static boolean openCreateAuction(GuiScreenEvent event, FlipItem item) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiChest) {
            IInventory chest = GuiUtil.getInventory(event.gui);
            if (chest.getStackInSlot(0) == null) {
                return false;
            }
            if (chest.getDisplayName().getUnformattedText().equals("Manage Auctions")) {
                DelayUtils.delayAction(800, () -> {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> createAuction((GuiScreenEvent) guiScreenEvent, item), "AutoList");

                    boolean foundSlot = false;
                    for (int i = 0; i < 5; i++) {
                        int slotIndex = 24 + (i * 9);

                        if (chest.getStackInSlot(slotIndex) != null && chest.getStackInSlot(slotIndex).getItem().equals(Items.golden_horse_armor)) {
                            GuiUtil.singleClick(slotIndex);
                            foundSlot = true;
                            break;
                        }
                    }

                    if (!foundSlot) {
                        BugLogger.logChat("Auction House Full!", true);

                        DiscordIntegration.sendToWebsocket("AuctionHouseFull", "");

                        Main.mc.thePlayer.closeScreen();
                        listingInv = false;
                        finishCurrentListing();
                    }
                });
                return true;
            } else if (chest.getDisplayName().getUnformattedText().contains("Create")) {
                return createAuction(event, item);
            }
        }
        return false;
    }

    public static boolean createAuction(GuiScreenEvent event, FlipItem item) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiChest) {
            IInventory chest = GuiUtil.getInventory(event.gui);
            if (chest.getStackInSlot(0) == null || chest.getStackInSlot(13) == null) {
                return false;
            }

            if (chest.getDisplayName().getUnformattedText().equals("Create Auction")) {
                DelayUtils.delayAction(800, () -> {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> openCreateAuction((GuiScreenEvent) guiScreenEvent, item), "AutoList");
                    GuiUtil.singleClick(48);
                });
                return false;
            } else if (chest.getDisplayName().getUnformattedText().equals("Create BIN Auction")) {
                if (!chest.getStackInSlot(13).getItem().equals(Item.getItemFromBlock(Blocks.stone_button))) {
                    GuiUtil.singleClick(13);
                    BugLogger.logChat("Item in slot already! Removing", true);

                    DelayUtils.delayAction(800, () -> {
                        RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> createAuction((GuiScreenEvent) guiScreenEvent, item), "AutoList");
                    });
                    return true;
                }

                ItemStack[] inventory = Main.mc.thePlayer.inventory.mainInventory;
                for (int i = 0; i < inventory.length; i++) {
                    if (inventory[i] != null) {
                        if (ChatUtils.stripColor(inventory[i].getDisplayName()).equals(item.strippedDisplayName)) {
                            int finalI = i;
                            DelayUtils.delayAction(800, () -> {
                                RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> openPrice((GuiScreenEvent) guiScreenEvent, item), "AutoList");
                                int clickId = finalI;
                                if (finalI < 9) {
                                    clickId += 81;
                                } else {
                                    clickId += 45;
                                }

                                GuiUtil.singleClick(clickId);
                            });
                            return true;
                        }
                    }
                }
                BugLogger.log("Item not found in inventory. Leaving Menu", FlipConfig.debug);
                BugLogger.logChat("Item not found in inventory. Leaving Menu", true);
                Main.mc.thePlayer.closeScreen();
                finishCurrentListing();
                return true;
            }
        }
        return false;
    }

    public static boolean openPrice(GuiScreenEvent event, FlipItem item) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiChest) {
            IInventory chest = GuiUtil.getInventory(event.gui);
            if (chest.getStackInSlot(0) == null) {
                return false;
            }

            if (chest.getDisplayName().getUnformattedText().equals("Create BIN Auction")) {
                DelayUtils.delayAction(800, () -> {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> setPrice((GuiScreenEvent) guiScreenEvent, item), "AutoList");
                    GuiUtil.singleClick(31);
                });
                return true;
            }
        }
        return false;
    }

    public static boolean setPrice(GuiScreenEvent event, FlipItem item) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiEditSign) {
            try {
                TileEntitySign tileEntitySign = ((GuiEditSignAccessor) event.gui).getTileSign();
                if (tileEntitySign == null) {
                    return false;
                }

                tileEntitySign.signText[0] = new ChatComponentText("" + item.sellPrice);

                DelayUtils.delayAction(800, () -> {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> openTime((GuiScreenEvent) guiScreenEvent, item), "AutoList");
                    Main.mc.currentScreen.onGuiClosed();
                });
                return true;
            } catch (Exception e) {
                BugLogger.logError(e);
            }
        }
        return false;
    }

    public static boolean openTime(GuiScreenEvent event, FlipItem item) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiChest) {
            IInventory chest = GuiUtil.getInventory(event.gui);
            if (chest.getStackInSlot(0) == null) {
                return false;
            }

            if (chest.getDisplayName().getUnformattedText().equals("Create BIN Auction")) {
                DelayUtils.delayAction(800, () -> {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> setCustomTime((GuiScreenEvent) guiScreenEvent, item), "AutoList");
                    GuiUtil.singleClick(33);
                });
                return true;
            }
        }
        return false;
    }

    public static boolean setCustomTime(GuiScreenEvent event, FlipItem item) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiChest) {
            IInventory chest = GuiUtil.getInventory(event.gui);
            if (chest.getStackInSlot(0) == null) {
                return false;
            }

            if (chest.getDisplayName().getUnformattedText().equals("Auction Duration")) {
                DelayUtils.delayAction(800, () -> {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> setTime((GuiScreenEvent) guiScreenEvent, item), "AutoList");
                    GuiUtil.singleClick(16);
                });
                return true;
            }
        }
        return false;
    }

    public static boolean setTime(GuiScreenEvent event, FlipItem item) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiEditSign) {
            TileEntitySign tileEntitySign = ((GuiEditSignAccessor) event.gui).getTileSign();
            if (tileEntitySign == null) {
                return false;
            }

            tileEntitySign.signText[0] = new ChatComponentText("" + FlipConfig.autoSellTime);

            DelayUtils.delayAction(800, () -> {
                RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> createBINAuction((GuiScreenEvent) guiScreenEvent, item), "AutoList");
                Main.mc.currentScreen.onGuiClosed();
            });
            return true;
        }
        return false;
    }

    public static boolean createBINAuction(GuiScreenEvent event, FlipItem item) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiChest) {
            IInventory chest = GuiUtil.getInventory(event.gui);
            if (chest.getStackInSlot(0) == null) {
                return false;
            }

            if (chest.getDisplayName().getUnformattedText().equals("Create BIN Auction")) {
                DelayUtils.delayAction(800, () -> {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> confirmAuction((GuiScreenEvent) guiScreenEvent, item), "AutoList");
                    GuiUtil.singleClick(29);
                });
                return true;
            }
        }
        return false;
    }

    public static boolean confirmAuction(GuiScreenEvent event, FlipItem item) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiChest) {
            IInventory chest = GuiUtil.getInventory(event.gui);
            if (chest.getStackInSlot(0) == null) {
                return false;
            }

            if (chest.getDisplayName().getUnformattedText().equals("Confirm BIN Auction")) {
                DelayUtils.delayAction(800, () -> {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> closeAuction((GuiScreenEvent) guiScreenEvent, item), "AutoList");
                    GuiUtil.singleClick(11);
                });
                return true;
            }
        }
        return false;
    }

    public static boolean closeAuction(GuiScreenEvent event, FlipItem item) {
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
                DelayUtils.delayAction(800, () -> {
                    Main.mc.thePlayer.closeScreen();
                    RealtimeEventRegistry.clearClazzMap("AutoList");
                    finishCurrentListing();
                    if (!listingInv) {
                        if (FlipConfig.listedWebhooks) {
                            DiscordIntegration.sendToWebsocket("FlipListed", item.serialize().toString());
                        }
                    }
                });
                return true;
            }
        }
        return false;
    }

    public static float calculateBINTax(float itemPrice) {
        float initialFee;
        float collectionFee = 0;

        // Determine initial fee based on item price
        if (itemPrice > 100000000) {
            initialFee = itemPrice * 0.025f;
        } else if (itemPrice >= 10000000) {
            initialFee = itemPrice * 0.02f;
        } else {
            initialFee = itemPrice * 0.01f;
        }

        // Determine collection fee if item price is above 1 million
        if (itemPrice > 1000000) {
            collectionFee = itemPrice * 0.01f;
        }

        // Calculate total tax
        return initialFee + collectionFee;
    }
}