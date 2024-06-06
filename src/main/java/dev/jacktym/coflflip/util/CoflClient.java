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
        try {
            if (!message.startsWith("Received: ")) {
                return;
            }
            message = message.split("Received: ")[1];

            try {
                RealtimeEventRegistry.handleMessage("coflMessage", message, 0);
                DiscordIntegration.getCoflCaptcha(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
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

            try {
                RealtimeEventRegistry.handleMessage("coflJson", strippedData, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (type.equals("flip")) {
                System.out.println(data.toString());
                JsonObject flip = data.getAsJsonObject();
                System.out.println("COFLFLIP: " + flip);
                String auctionId = flip.get("id").getAsString();
                if (!auctionUuids.contains(auctionId)) {
                    auctionUuids += auctionId;
                    FlipItem item = new FlipItem();
                    item.auctionId = auctionId;
                    item.coflWorth = flip.get("target").getAsLong();
                    if (flip.get("auction").getAsJsonObject().has("uuid")) {
                        item.uuid = flip.get("auction").getAsJsonObject().get("uuid").getAsString();
                    }
                    item.sellerUuid = flip.get("auction").getAsJsonObject().get("auctioneerId").getAsString();
                    item.skyblockId = flip.get("auction").getAsJsonObject().get("tag").getAsString();
                    item.bed = false;
                    item.auctionStart = DateTimeFormatter.ISO_INSTANT.parse(flip.get("auction").getAsJsonObject().get("start").getAsString(), Instant::from).toEpochMilli();
                    AutoOpen.openAuction(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
