package dev.jacktym.marketshark.util;

import com.google.gson.*;
import dev.jacktym.marketshark.Main;
import dev.jacktym.marketshark.config.FlipConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlipItem {
    public static Map<String, FlipItem> flipMap = new HashMap<>();
    public static List<FlipItem> flipItems = new ArrayList<>();
    public String displayName;
    public String strippedDisplayName;
    public ItemStack itemStack;
    public String uuid;
    public long buyPrice;
    public long coflWorth;
    public long sellPrice;
    public long startTime;
    public long buyTime;
    public int buySpeed;
    public long auctionStart;
    public String auctionId;
    public String username;
    public String sellerUuid;
    public String skyblockId;
    public boolean bed;
    public String buyer;
    public boolean sold;
    public boolean bought;
    public String skipReason;
    public int buyClicks = 0;
    public boolean closed = false;
    public String finder;
    public boolean bedClicking = false;
    //#if >=Megalodon
    public boolean skipped = false;
    //#endif >=Megalodon

    public FlipItem() {
        flipItems.add(this);
        this.username = Main.mc.getSession().getUsername();
    }

    public static FlipItem getFlipItem(ItemStack itemStack) {
        String uuid = getUuid(itemStack);
        if (uuid != null && !uuid.isEmpty()) {
            FlipItem existingItem = flipMap.get(uuid);
            if (existingItem != null) {
                return existingItem;
            }
        }
        FlipItem item = new FlipItem();
        item.setItemStack(itemStack);
        return item;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.displayName = this.itemStack.getDisplayName();
        this.strippedDisplayName = ChatUtils.stripColor(this.displayName);
        this.uuid = getUuid(this.itemStack);
        flipMap.put(uuid, this);
    }

    public JsonObject serialize() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("displayName", this.displayName);
        jsonObject.addProperty("strippedDisplayName", this.strippedDisplayName);
        jsonObject.addProperty("uuid", this.uuid);
        jsonObject.addProperty("buyPrice", this.buyPrice);
        jsonObject.addProperty("sellPrice", this.sellPrice);
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
        jsonObject.addProperty("buyer", this.buyer);
        jsonObject.addProperty("sold", this.sold);
        jsonObject.addProperty("bought", this.bought);
        jsonObject.addProperty("skipReason", this.skipReason);
        jsonObject.addProperty("sendBought", FlipConfig.boughtWebhooks);
        jsonObject.addProperty("sendSold", FlipConfig.soldWebhooks);
        jsonObject.addProperty("finder", this.finder);
        return jsonObject;
    }

    public static String getUuid(ItemStack stack) {
        if (stack != null) {
            NBTBase uuidTag = stack.serializeNBT().getCompoundTag("tag").getCompoundTag("ExtraAttributes").getTag("uuid");
            if (uuidTag != null) {
                return uuidTag.toString().replace("\"", "");
            }
        }
        return "";
    }

    public static void saveFlipData() {
        JsonArray jsonArray = new JsonArray();
        for (FlipItem item : flipItems) {
            if (item.bought && !item.sold) {
                jsonArray.add(item.serialize());
            }
        }

        try (FileWriter file = new FileWriter("config/flipitems.json")) {
            file.write(jsonArray.toString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadFlipData() {
        try (FileReader reader = new FileReader("config/flipitems.json")) {
            JsonArray jsonArray = new JsonParser().parse(reader).getAsJsonArray();
            for (JsonElement element : jsonArray) {
                JsonObject jsonObject = element.getAsJsonObject();
                FlipItem item = new FlipItem();
                item.displayName = !jsonObject.get("displayName").isJsonNull()
                        ? jsonObject.get("displayName").getAsString() : "";
                item.strippedDisplayName = !jsonObject.get("strippedDisplayName").isJsonNull()
                        ? jsonObject.get("strippedDisplayName").getAsString() : "";
                item.uuid = !jsonObject.get("uuid").isJsonNull()
                        ? jsonObject.get("uuid").getAsString() : "";
                item.buyPrice = !jsonObject.get("buyPrice").isJsonNull()
                        ? jsonObject.get("buyPrice").getAsLong() : 0L;
                item.sellPrice = !jsonObject.get("sellPrice").isJsonNull()
                        ? jsonObject.get("sellPrice").getAsLong() : 0L;
                item.coflWorth = !jsonObject.get("coflWorth").isJsonNull()
                        ? jsonObject.get("coflWorth").getAsLong() : 0L;
                item.startTime = !jsonObject.get("startTime").isJsonNull()
                        ? jsonObject.get("startTime").getAsLong() : 0L;
                item.buyTime = !jsonObject.get("buyTime").isJsonNull()
                        ? jsonObject.get("buyTime").getAsLong() : 0L;
                item.buySpeed = !jsonObject.get("buySpeed").isJsonNull()
                        ? jsonObject.get("buySpeed").getAsInt() : 0;
                item.auctionStart = !jsonObject.get("auctionStart").isJsonNull()
                        ? jsonObject.get("auctionStart").getAsLong() : 0L;
                item.auctionId = !jsonObject.get("auctionId").isJsonNull()
                        ? jsonObject.get("auctionId").getAsString() : "";
                item.username = !jsonObject.get("username").isJsonNull()
                        ? jsonObject.get("username").getAsString() : "";
                item.sellerUuid = !jsonObject.get("sellerUuid").isJsonNull()
                        ? jsonObject.get("sellerUuid").getAsString() : "";
                item.skyblockId = !jsonObject.get("skyblockId").isJsonNull()
                        ? jsonObject.get("skyblockId").getAsString() : "";
                item.bed = !jsonObject.get("bed").isJsonNull() && jsonObject.get("bed").getAsBoolean();
                item.buyer = !jsonObject.get("buyer").isJsonNull()
                        ? jsonObject.get("buyer").getAsString() : "";
                item.sold = !jsonObject.get("sold").isJsonNull() && jsonObject.get("sold").getAsBoolean();
                item.bought = !jsonObject.get("bought").isJsonNull() && jsonObject.get("bought").getAsBoolean();
                item.finder = jsonObject.has("finder") && !jsonObject.get("finder").isJsonNull()
                        ? jsonObject.get("finder").getAsString() : "";
                if (item.bought && !item.sold) {
                    if (!item.uuid.isEmpty()) {
                        flipMap.put(item.uuid, item);
                    }
                } else {
                    // Flips automatically added when new FlipItem created
                    flipItems.remove(item);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getItemStrings() {
        StringBuilder sb = new StringBuilder();
        // Avoid ConcurrentModificationException
        for (FlipItem item : new ArrayList<>(flipItems)) {
            sb.append(item.serialize().toString()).append(",");
        }
        return sb.toString();
    }
}
