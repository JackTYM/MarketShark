package dev.jacktym.coflflip.macros;

import dev.jacktym.coflflip.Main;
import dev.jacktym.coflflip.config.FlipConfig;
import dev.jacktym.coflflip.mixins.GuiEditSignAccessor;
import dev.jacktym.coflflip.util.*;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
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
        if (currentlyListing) {
            return;
        }
        currentlyListing = true;
        listingInv = true;
        new Thread(() -> {
            try {
                ItemStack[] inventory = Main.mc.thePlayer.inventory.mainInventory;
                for (int i = 0; i < inventory.length; i++) {
                    if (i == 8) {
                        // Skyblock Menu
                        continue;
                    }
                    if (inventory[i] != null) {
                        Main.mc.thePlayer.sendChatMessage("/ah");
                        int finalI = i;
                        RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> openManageAuctions((GuiScreenEvent) guiScreenEvent, inventory[finalI]), "AutoList");
                        waitForListingToFinish();
                        currentlyListing = true;
                        System.out.println(currentlyListing + " | " + listingInv);
                        if (!listingInv) {
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                finishCurrentListing();
            }
        }).start();
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

    public static void listItem(ItemStack item, boolean coflValue) {
        if (!FlipConfig.autoSell || item == null || currentlyListing) {
            return;
        }

        QueueUtil.addToQueue(() -> {
            currentlyListing = true;
            Main.mc.thePlayer.sendChatMessage("/ah");
            RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> openManageAuctions((GuiScreenEvent) guiScreenEvent, item), "AutoList");
        });
    }

    public static boolean openManageAuctions(GuiScreenEvent event, ItemStack item) {
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
                DelayUtils.delayAction(300, () -> {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> openCreateAuction((GuiScreenEvent) guiScreenEvent, item), "AutoList");
                    GuiUtil.tryClick(15);
                });
                return true;
            }
        }
        return false;
    }

    public static boolean openCreateAuction(GuiScreenEvent event, ItemStack item) {
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
                DelayUtils.delayAction(300, () -> {
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

    public static boolean createAuction(GuiScreenEvent event, ItemStack item) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiChest) {
            IInventory chest = GuiUtil.getInventory(event.gui);
            if (chest.getStackInSlot(0) == null) {
                return false;
            }

            if (chest.getDisplayName().getUnformattedText().equals("Create Auction")) {
                DelayUtils.delayAction(300, () -> {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> openCreateAuction((GuiScreenEvent) guiScreenEvent, item), "AutoList");
                    GuiUtil.singleClick(48);
                });
                return false;
            } else if (chest.getDisplayName().getUnformattedText().equals("Create BIN Auction")) {
                ItemStack[] inventory = Main.mc.thePlayer.inventory.mainInventory;
                for (int i = 0; i < inventory.length; i++) {
                    if (inventory[i] != null) {
                        if (ChatUtils.stripColor(inventory[i].getDisplayName()).equals(ChatUtils.stripColor(item.getDisplayName()))) {
                            int finalI = i;
                            DelayUtils.delayAction(300, () -> {
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
                System.out.println("Item not found in inventory. Leaving Menu");
                Main.mc.thePlayer.closeScreen();
                finishCurrentListing();
                return true;
            }
        }
        return false;
    }

    public static boolean openPrice(GuiScreenEvent event, ItemStack item) {
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
                DelayUtils.delayAction(300, () -> {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> setPrice((GuiScreenEvent) guiScreenEvent, item), "AutoList");
                    GuiUtil.singleClick(31);
                });
                return true;
            }
        }
        return false;
    }

    public static boolean setPrice(GuiScreenEvent event, ItemStack item) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiEditSign) {
            TileEntitySign tileEntitySign = ((GuiEditSignAccessor) event.gui).getTileSign();
            if (tileEntitySign == null) {
                return false;
            }

            String price = CoflAPIUtil.getCoflPrice(item);

            tileEntitySign.signText[0] = new ChatComponentText(price);

            DelayUtils.delayAction(300, () -> {
                if (tileEntitySign.signText[0].getUnformattedText().equals(price)) {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> openTime((GuiScreenEvent) guiScreenEvent, item), "AutoList");
                    Main.mc.currentScreen.onGuiClosed();
                }
            });
            return true;
        }
        return false;
    }

    public static boolean openTime(GuiScreenEvent event, ItemStack item) {
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
                DelayUtils.delayAction(300, () -> {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> setCustomTime((GuiScreenEvent) guiScreenEvent, item), "AutoList");
                    GuiUtil.singleClick(33);
                });
                return true;
            }
        }
        return false;
    }

    public static boolean setCustomTime(GuiScreenEvent event, ItemStack item) {
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
                DelayUtils.delayAction(300, () -> {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> setTime((GuiScreenEvent) guiScreenEvent, item), "AutoList");
                    GuiUtil.singleClick(16);
                });
                return true;
            }
        }
        return false;
    }

    public static boolean setTime(GuiScreenEvent event, ItemStack item) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiEditSign) {
            TileEntitySign tileEntitySign = ((GuiEditSignAccessor) event.gui).getTileSign();
            if (tileEntitySign == null) {
                return false;
            }

            tileEntitySign.signText[0] = new ChatComponentText("336");

            DelayUtils.delayAction(300, () -> {
                if (tileEntitySign.signText[0].getUnformattedText().equals("336")) {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> createBINAuction((GuiScreenEvent) guiScreenEvent, item), "AutoList");
                    Main.mc.currentScreen.onGuiClosed();
                }
            });
            return true;
        }
        return false;
    }

    public static boolean createBINAuction(GuiScreenEvent event, ItemStack item) {
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
                DelayUtils.delayAction(300, () -> {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> confirmAuction((GuiScreenEvent) guiScreenEvent, item), "AutoList");
                    GuiUtil.tryClick(29);
                });
                return true;
            }
        }
        return false;
    }

    public static boolean confirmAuction(GuiScreenEvent event, ItemStack item) {
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
                DelayUtils.delayAction(300, () -> {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> closeAuction((GuiScreenEvent) guiScreenEvent, item), "AutoList");
                    GuiUtil.tryClick(11);
                });
                return true;
            }
        }
        return false;
    }

    public static boolean closeAuction(GuiScreenEvent event, ItemStack item) {
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
                    Main.mc.thePlayer.closeScreen();
                    finishCurrentListing();
                    QueueUtil.finishAction();
                    RealtimeEventRegistry.clearClazzMap("AutoList");
                });
                return true;
            }
        }
        return false;
    }
}
