package dev.jacktym.coflflip.macros;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.jacktym.coflflip.Main;
import dev.jacktym.coflflip.config.FlipConfig;
import dev.jacktym.coflflip.mixins.GuiEditSignAccessor;
import dev.jacktym.coflflip.util.*;
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
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AutoList {
    private static boolean currentlyListing = false;
    public static boolean listingInv = false;
    private static final Lock lock = new ReentrantLock();
    private static final Condition listingCondition = lock.newCondition();

    public static void listInventory() {
        if (currentlyListing) {
            return;
        }
        QueueUtil.addToQueue(() -> {
            currentlyListing = true;
            listingInv = true;
            new Thread(() -> {
                try {
                    ItemStack[] inventory = Main.mc.thePlayer.inventory.mainInventory;
                    JsonArray coflPrices = CoflAPIUtil.getCoflPrices(inventory);
                    if (coflPrices != null) {
                        for (int i = 0; i < inventory.length; i++) {
                            if (i == 8) {
                                // Skyblock Menu
                                continue;
                            }
                            if (inventory[i] != null) {
                                FlipItem item = FlipItem.getFlipItem(inventory[i]);

                                switch (FlipConfig.autoSellPrice) {
                                    case 0:
                                        item.sellPrice = coflPrices.get(i).getAsJsonObject().get("lbin").getAsLong();
                                        break;
                                    case 1:
                                        item.sellPrice = (long) (coflPrices.get(i).getAsJsonObject().get("lbin").getAsLong() * 0.95);
                                        break;
                                    case 4:
                                        if (item.coflWorth != 0) {
                                            item.sellPrice = item.coflWorth;
                                            break;
                                        }
                                        ChatUtils.printMarkedChat("No Cofl Flip to use. Defaulting to Median Price");
                                    case 2:
                                        item.sellPrice = coflPrices.get(i).getAsJsonObject().get("median").getAsLong();
                                        break;
                                    case 3:
                                        item.sellPrice = (long) (coflPrices.get(i).getAsJsonObject().get("median").getAsLong() * 0.95);
                                        break;
                                }

                                FlipItem finalItem = item;
                                RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> openManageAuctions((GuiScreenEvent) guiScreenEvent, finalItem), "AutoList");
                                Main.mc.thePlayer.sendChatMessage("/ah");
                                waitForListingToFinish();
                                currentlyListing = true;
                                if (!listingInv) {
                                    QueueUtil.finishAction();
                                    break;
                                }
                            }
                        }
                    } else {
                        ChatUtils.printMarkedChat("Failed to fetch Cofl API. Rate Limited?");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finishCurrentListing();
                    QueueUtil.finishAction();
                }
            }).start();
        });
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

    public static void listItem(FlipItem item) {
        if (!FlipConfig.autoSell || item == null || currentlyListing) {
            return;
        }

        if (FlipConfig.autoSellPrice == 4 && item.coflWorth != 0) {
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
                        ChatUtils.printMarkedChat("No Cofl Flip to use. Defaulting to Median Price");
                    case 2:
                        item.sellPrice = coflPrice.get("median").getAsLong();
                        break;
                    case 3:
                        item.sellPrice = (long) (coflPrice.get("median").getAsLong() * 0.95);
                        break;
                }
            } else {
                ChatUtils.printMarkedChat("Failed to fetch Cofl API. Rate Limited?");
            }
        }

        if (item.sellPrice < item.buyPrice) {
            ChatUtils.printMarkedChat("Skipped listing item. Worth lower than buy price!");

            RealtimeEventRegistry.clearClazzMap("AutoList");
            finishCurrentListing();
            QueueUtil.finishAction();

            if (FlipConfig.listedWebhooks) {
                DiscordIntegration.sendToWebsocket("ListingSkipped", item.serialize().toString());
            }
            return;
        }

        QueueUtil.addToQueue(() -> {
            currentlyListing = true;
            Main.mc.thePlayer.sendChatMessage("/ah");
            RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> openManageAuctions((GuiScreenEvent) guiScreenEvent, item), "AutoList");

            
            RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> Failsafes.closeGuiFailsafe((GuiScreenEvent) guiScreenEvent, "AutoList"), "AutoList");
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

            if (chest.getDisplayName().getUnformattedText().equals("Co-op Auction House")) {
                DelayUtils.delayAction(800, () -> {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> openCreateAuction((GuiScreenEvent) guiScreenEvent, item), "AutoList");
                    GuiUtil.tryClick(15);
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
                    if (chest.getStackInSlot(24) != null && chest.getStackInSlot(24).getItem().equals(Items.golden_horse_armor)) {
                        GuiUtil.tryClick(24);
                    } else {
                        if (chest.getStackInSlot(33) != null && !chest.getStackInSlot(33).getTagCompound().getCompoundTag("display").getTagList("Lore", 8).get(0).toString().contains("You reached the maximum number of")) {
                            GuiUtil.tryClick(33);
                        } else {
                            if (FlipConfig.debug) {
                                System.out.println("Auction House Full!");
                            }
                            ChatUtils.printMarkedChat("Auction House Full!");

                            Main.mc.thePlayer.closeScreen();
                            listingInv = false;
                            finishCurrentListing();
                        }
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
                    ChatUtils.printMarkedChat("Item in slot already! Removing");

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
                if (FlipConfig.debug) {
                    System.out.println("Item not found in inventory. Leaving Menu");
                }
                ChatUtils.printMarkedChat("Item not found in inventory. Leaving Menu");
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
            TileEntitySign tileEntitySign = ((GuiEditSignAccessor) event.gui).getTileSign();
            if (tileEntitySign == null) {
                return false;
            }

            tileEntitySign.signText[0] = new ChatComponentText("" + item.sellPrice);

            DelayUtils.delayAction(800, () -> {
                if (tileEntitySign.signText[0].getUnformattedText().equals("" + item.sellPrice)) {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> openTime((GuiScreenEvent) guiScreenEvent, item), "AutoList");
                    Main.mc.currentScreen.onGuiClosed();
                }
            });
            return true;
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

            tileEntitySign.signText[0] = new ChatComponentText(FlipConfig.autoSellTime);

            DelayUtils.delayAction(800, () -> {
                if (tileEntitySign.signText[0].getUnformattedText().equals(FlipConfig.autoSellTime)) {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> createBINAuction((GuiScreenEvent) guiScreenEvent, item), "AutoList");
                    Main.mc.currentScreen.onGuiClosed();
                }
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
                    GuiUtil.tryClick(29);
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
                    GuiUtil.tryClick(11);
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
                        QueueUtil.finishAction();

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
}
