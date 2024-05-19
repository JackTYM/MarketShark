package dev.jacktym.coflflip.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.jacktym.coflflip.Main;
import dev.jacktym.coflflip.config.FlipConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.UUID;

public class CoflWebsocketClient {
    public WebsocketClient websocketClient;
    private static String URL;

    public CoflWebsocketClient(boolean usServers) {
        URL = "/modsocket?player=" + Minecraft.getMinecraft().getSession().getUsername() + "&version=1.5.5-Alpha&SId=" + getSessionID();
        try {
            this.websocketClient = new WebsocketClient(new URI(usServers ? "ws://sky-us.coflnet.com" + URL : "wss://sky.coflnet.com" + URL), this::onOpen, this::onMessage, this::onClose, this::onError);
        } catch (Exception e) {
            System.out.println("Failed to connect to websocket. Switching to backup servers");
            try {
                this.websocketClient = new WebsocketClient(new URI("ws://sky-mod.coflnet.com" + URL), this::onOpen, this::onMessage, this::onClose, this::onError);
            } catch (Exception ex) {
                System.out.println("Failed to connect to backup websocket. Quitting flipper");
            }
        }
        RealtimeEventRegistry.eventMap.put("entityJoinWorldEvent", this::entityJoinWorldEvent);
    }

    public CoflWebsocketClient() {
        new CoflWebsocketClient(false);
    }

    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("[CoflFlip] Connected to Cofl!");
        if (FlipConfig.debug) {
            System.out.println(serverHandshake.getHttpStatusMessage());
        }
    }

    public void onMessage(String message) {
        JsonObject jsonObject = new JsonParser().parse(message).getAsJsonObject();
        String type = jsonObject.get("type").toString();
        JsonObject data = jsonObject.getAsJsonObject("data");

        if (FlipConfig.debug) {
            StringBuilder dataString = new StringBuilder();
            System.out.println(jsonObject);
            dataString.append("[").append(type).append("] ");
            if (jsonObject.get("data").isJsonArray()) {
                for (JsonElement element : data.getAsJsonArray()) {
                    dataString.append(ChatUtils.stripColor(element.getAsJsonObject().get("text").toString())).append("\n");
                }
            } else {
                dataString.append(ChatUtils.stripColor(jsonObject.get("data").getAsJsonObject().get("text").toString()));
            }
            System.out.println(dataString);
        }

        switch (jsonObject.get("type").toString()) {
            case "writeToChat": {
                handleMessage(data);
                break;
            }
            case "chatMessage": {
                for (JsonElement element : data.getAsJsonArray()) {
                    handleMessage(element.getAsJsonObject());
                }
            }
        }
    }

    public void onClose(String reason) {
        System.out.println("Failed to connect to websocket. Switching to backup servers");
        try {
            this.websocketClient = new WebsocketClient(new URI("ws://sky-mod.coflnet.com" + URL), this::onOpen, this::onMessage, this::onClose, this::onError);
        } catch (Exception ex) {
            System.out.println("Failed to connect to backup websocket. Quitting flipper");
        }
        this.websocketClient.connect();
    }

    public void onError(Exception ex) {

    }

    private static String getSessionID() {
        if (FlipConfig.SId.isEmpty() || FlipConfig.sessionExpiresIn.isEmpty() || Long.parseLong(FlipConfig.sessionExpiresIn) <= System.currentTimeMillis()) {
            FlipConfig.SId = UUID.randomUUID().toString();
            FlipConfig.sessionExpiresIn = "" + (System.currentTimeMillis() + +1000L * 60 * 60 * 24 * 180);
        }

        return FlipConfig.SId;
    }

    private static void handleMessage(JsonObject data) {
        String text = data.get("text").toString();
        String onClick = data.get("onClick").toString();
        ChatStyle style = new ChatStyle();

        if (!onClick.equals("null")) {
            if (onClick.startsWith("http")) {
                style.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, onClick));
            } else {
                style.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, onClick));
            }
        }
        ChatUtils.addChatMessage(new ChatComponentText(text).setChatStyle(style));
    }

    public void entityJoinWorldEvent(EntityJoinWorldEvent event){
        if (event.entity.equals(Main.mc.thePlayer)) {
            this.websocketClient.reconnect();
        }
    }
}
