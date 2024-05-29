package dev.jacktym.coflflip.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.jacktym.coflflip.macros.AutoOpen;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class CoflClient {
    public static String auctionUuids = "";

    public static void handleMessage(String message) {
        if (!message.startsWith("Received: ")) {
            return;
        }
        message = message.split("Received: ")[1];

        JsonObject jsonObject = new JsonParser().parse(message).getAsJsonObject();
        String type = jsonObject.get("type").toString().replace("\"", "");
        String dataStr = jsonObject.get("data").toString();
        String strippedData = dataStr
                .substring(1, dataStr.length() - 1)
                .replace("\\\"", "\"")
                .replace("\":\"\\\\n", "\":\"")
                .replace("\\\\n", "\n")
                .replace("\\n", "\n");

        JsonElement data = new JsonParser().parse(strippedData);

        if (type.equals("flip")) {
            JsonObject flip = data.getAsJsonObject();
            String auctionId = flip.get("id").getAsString();
            if (!auctionUuids.contains(auctionId)) {
                auctionUuids += auctionId;
                FlipItem item = new FlipItem();
                item.auctionId = auctionId;
                item.coflWorth = flip.get("target").getAsLong();
                item.uuid = flip.get("auction").getAsJsonObject().get("uuid").getAsString();
                item.sellerUuid = flip.get("auction").getAsJsonObject().get("auctioneerId").getAsString();
                item.skyblockId = flip.get("auction").getAsJsonObject().get("tag").getAsString();
                item.bed = false;
                item.auctionStart = DateTimeFormatter.ISO_INSTANT.parse(flip.get("auction").getAsJsonObject().get("start").getAsString(), Instant::from).toEpochMilli();
                AutoOpen.openAuction(item);
            }
        }
    }
}
