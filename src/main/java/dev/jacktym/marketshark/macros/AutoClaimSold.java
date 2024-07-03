package dev.jacktym.marketshark.macros;

import dev.jacktym.marketshark.Main;
import dev.jacktym.marketshark.config.FlipConfig;
import dev.jacktym.marketshark.util.*;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class AutoClaimSold {
    @SubscribeEvent
    public void clientChatReceivedEvent(ClientChatReceivedEvent event) {
        if (!FlipConfig.autoClaimSold || Main.paused) {
            return;
        }

        String message = ChatUtils.stripColor(event.message.getUnformattedText());

        if (message.startsWith("[Auction]") && message.contains("bought")) {
            QueueUtil.addToQueue(() -> {
                RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> claimAuction((GuiScreenEvent) guiScreenEvent), "AutoClaimSold");
                Main.mc.thePlayer.sendChatMessage(event.message.getChatStyle().getChatClickEvent().getValue());
                
                
            });
        }
    }

    private static List<FlipItem> currentAuctionHouse = new ArrayList<>();

    @SubscribeEvent
    public void checkForSold(GuiScreenEvent event) {
        if (Main.paused) {
            return;
        }
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return;
        }
        if (event.gui instanceof GuiChest) {
            IInventory chest = GuiUtil.getInventory(event.gui);
            if (chest.getStackInSlot(0) == null) {
                return;
            }

            if (ChatUtils.stripColor(chest.getDisplayName().getUnformattedText()).equals("Manage Auctions")) {
                for (int i = 1; i <= 35; i++) {
                    int slotId = i + 9;

                    int increment = (i - 1) / 7;
                    slotId += increment * 2;

                    ItemStack stack = chest.getStackInSlot(slotId);
                    if (stack != null) {
                        if (stack.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane)) {
                            break;
                        }

                        FlipItem item = getItemFromAuction(stack);

                        if (!currentAuctionHouse.contains(item))
                            currentAuctionHouse.add(item);
                    }
                }
            } else if (ChatUtils.stripColor(chest.getDisplayName().getUnformattedText()).equals("BIN Auction View")) {
                ItemStack soldItem = chest.getStackInSlot(13);
                if (soldItem == null) {
                    return;
                }

                FlipItem item = getItemFromAuction(soldItem);

                if (!currentAuctionHouse.contains(item))
                    currentAuctionHouse.add(item);
            }
        }
    }

    public static boolean claimAuction(GuiScreenEvent event) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiChest) {
            IInventory chest = GuiUtil.getInventory(event.gui);
            if (chest.getStackInSlot(0) == null) {
                return false;
            }

            ItemStack soldItem = chest.getStackInSlot(13);
            if (soldItem == null) {
                return false;
            }

            if (chest.getDisplayName().getUnformattedText().equals("BIN Auction View")) {
                DelayUtils.delayAction(300, () -> {
                    GuiUtil.singleClick(31);
                    
                    RealtimeEventRegistry.clearClazzMap("AutoClaimSold");
                });
                return true;
            }
        }
        return false;
    }

    public static FlipItem getItemFromAuction(ItemStack stack) {
        FlipItem item = FlipItem.getFlipItem(stack);

        NBTTagList tagList = stack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);

        int tagIndex = 0;
        int consecutiveEmpty = 0;
        String seller = "";
        long binPrice = 0;
        while (true) {
            String line = ChatUtils.stripColor(tagList.getStringTagAt(tagIndex));
            if (line.isEmpty()) {
                consecutiveEmpty++;
                // 3+ empty lines in a row assuming end of Lore
                if (consecutiveEmpty > 2) {
                    break;
                }
            } else {
                consecutiveEmpty = 0;
            }

            if (line.contains("Seller: ")) {
                seller = line.split("Seller: ")[1].replaceAll("\\[.*?]", "").trim();
            }
            if (line.contains("Sold for: ")) {
                item.sellPrice = Long.parseLong(line.split("Sold for: ")[1].split(" coins")[0].replace(",", ""));
            }
            if (line.contains("Buy it now: ")) {
                binPrice = Long.parseLong(line.split("Buy it now: ")[1].split(" coins")[0].replace(",", ""));
                if (seller.equals(Main.mc.getSession().getUsername()) && binPrice != 0) {
                    item.sellPrice = binPrice;
                }
            }
            if (line.contains("Buyer: ")) {
                item.buyer = line.split("Buyer: ")[1].replaceAll("\\[.*?]", "").trim();
            }
            if (line.contains("Status: Sold!")) {
                if (!item.sold && seller.equals(Main.mc.getSession().getUsername())) {
                    item.sold = true;
                    DiscordIntegration.sendToWebsocket("FlipSold", item.serialize().toString());
                }
            }
            tagIndex++;
        }

        return item;
    }
}
