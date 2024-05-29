package dev.jacktym.coflflip.util;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;

import java.util.ArrayList;
import java.util.List;

public class FlipItem {
    public static List<FlipItem> flipItems = new ArrayList<>();
    public String displayName;
    public String strippedDisplayName;
    public ItemStack itemStack;
    public String uuid;
    public long buyPrice;
    public long coflWorth;
    public long startTime;
    public long buyTime;
    public int buySpeed;
    public long auctionStart;
    public String auctionId;
    public String username;
    public String sellerUuid;
    public String skyblockId;
    public boolean bed;

    public FlipItem() {
        flipItems.add(this);
        this.username = Minecraft.getMinecraft().getSession().getUsername();
    }

    public FlipItem(ItemStack itemStack) {
        this();
        setItemStack(itemStack);
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.displayName = this.itemStack.getDisplayName();
        this.strippedDisplayName = ChatUtils.stripColor(this.displayName);
        this.uuid = getUuid(this.itemStack);
    }

    public JsonObject serialize() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("displayName", this.displayName);
        jsonObject.addProperty("strippedDisplayName", this.strippedDisplayName);
        jsonObject.addProperty("uuid", this.uuid);
        jsonObject.addProperty("buyPrice", this.buyPrice);
        jsonObject.addProperty("coflWorth", this.coflWorth);
        jsonObject.addProperty("startTime", this.startTime);
        jsonObject.addProperty("buyTime", this.buyTime);
        jsonObject.addProperty("buySpeed", this.buySpeed);
        jsonObject.addProperty("auctionStart", this.auctionStart);
        jsonObject.addProperty("auctionId", this.auctionId);
        jsonObject.addProperty("username", this.username);
        jsonObject.addProperty("sellerUuid", this.sellerUuid);
        jsonObject.addProperty("skyblockId", this.skyblockId);
        jsonObject.addProperty("bed", this.bed);
        return jsonObject;
    }

    public static FlipItem getItemByUuid(String uuid) {
        for (FlipItem item : flipItems) {
            if (item.uuid.equals(uuid)) {
                return item;
            }
        }
        return null;
    }

    public static String getUuid(ItemStack stack) {
        if (stack != null) {
            NBTBase uuidTag = stack.serializeNBT().getCompoundTag("tag").getCompoundTag("ExtraAttributes").getTag("uuid");
            if (uuidTag != null) {
                return uuidTag.toString();
            }
        }
        return "";
    }
}
