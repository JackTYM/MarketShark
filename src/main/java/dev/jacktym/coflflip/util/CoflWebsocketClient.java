package dev.jacktym.coflflip.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.jacktym.coflflip.Main;
import dev.jacktym.coflflip.config.FlipConfig;
import dev.jacktym.coflflip.macros.AutoOpen;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.UUID;
import java.util.function.Consumer;

public class CoflWebsocketClient {
    public static WebsocketClient websocketClient;
    public static String URL;
    private boolean connected = false;

    public CoflWebsocketClient(boolean usServers) {
        URL = "/modsocket?player=" + Minecraft.getMinecraft().getSession().getUsername() + "&version=1.5.5-Alpha&SId=" + getSessionID();
        try {
            setWebsocketClient(new URI(usServers ? "ws://sky-us.coflnet.com" + URL : "wss://sky.coflnet.com" + URL), this::onOpen, this::onMessage, this::onClose, this::onError);
        } catch (Exception e) {
            System.out.println("Failed to connect to websocket. Switching to backup servers");
            try {
                setWebsocketClient(new URI("ws://sky-mod.coflnet.com" + URL), this::onOpen, this::onMessage, this::onClose, this::onError);
            } catch (Exception ex) {
                System.out.println("Failed to connect to backup websocket. Quitting flipper");
            }
        }
        RealtimeEventRegistry.registerEvent("entityJoinWorldEvent", event -> {
            entityJoinWorldEvent((EntityJoinWorldEvent) event);
            return false;
        });
    }

    public CoflWebsocketClient() {
        this(false);
    }

    public void setWebsocketClient(URI serverUri, Consumer<ServerHandshake> onOpen, Consumer<String> onMessage, Consumer<String> onClose, Consumer<Exception> onError) {
        if (websocketClient != null && websocketClient.isOpen()) {
            websocketClient.close();
        }
        websocketClient = new WebsocketClient(serverUri, onOpen, onMessage, onClose, onError);
    }

    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("[CoflFlip] Connected to Cofl!");
        if (FlipConfig.debug) {
            System.out.println(serverHandshake.getHttpStatusMessage());
        }
    }

    public void onMessage(String message) {
        JsonObject jsonObject = new JsonParser().parse(message).getAsJsonObject();
        String type = jsonObject.get("type").toString().replace("\"", "");
        String dataStr = jsonObject.get("data").toString();
        String strippedData = dataStr
                .substring(1, dataStr.length() - 1)
                .replace("\\\"", "\"");

        JsonElement data = new JsonParser().parse(strippedData);

        if (FlipConfig.debug) {
            StringBuilder dataString = new StringBuilder();
            dataString.append("[").append(type).append("] ");
            if (data.isJsonArray()) {
                for (JsonElement element : data.getAsJsonArray()) {
                    dataString.append(ChatUtils.stripColor(element.getAsJsonObject().get("text").toString())).append(" ");
                }
            } else {
                dataString.append(ChatUtils.stripColor(data.getAsJsonObject().get("text").toString()));
            }
            System.out.println(dataString);
        }

        if (data.toString().contains("What do you want to do?") && data.toString().contains("always ah flip")) {
            System.out.println("Starting");
            JsonObject json = new JsonObject();
            json.addProperty("type", "flip");
            json.addProperty("data", "/cofl flip");
            websocketClient.send(json.toString());
        }


        System.out.println(type);
        switch (type) {
            case "writeToChat":
                System.out.println(data);
                ChatUtils.addChatMessage(handleMessage(data.getAsJsonObject()));
                break;
            case "chatMessage":
            case "flip":
                System.out.println(data.getAsJsonArray());
                IChatComponent chatMessage = new ChatComponentText("");
                for (JsonElement element : data.getAsJsonArray()) {
                    chatMessage.appendSibling(handleMessage(element.getAsJsonObject()));
                }
                ChatUtils.addChatMessage(chatMessage);
                String onClick = data.getAsJsonArray().get(0).getAsJsonObject().get("onClick").toString().replace("\"", "");
                if (onClick.contains("/viewauction")) {
                    AutoOpen.openAuction(onClick);
                }
                break;
        }
    }

    public void onClose(String reason) {
        System.out.println("Websocket closed. " + reason);
        System.out.println("Failed to connect to websocket. Switching to backup servers");
        try {
            setWebsocketClient(new URI("ws://sky-mod.coflnet.com" + URL), this::onOpen, this::onMessage, this::onClose, this::onError);
        } catch (Exception ex) {
            System.out.println("Failed to connect to backup websocket. Quitting flipper");
        }
        websocketClient.connect();
    }

    public void onError(Exception ex) {

    }

    public void reconnect() {
        FlipConfig.SId = "";
        FlipConfig.sessionExpiresIn = "";
        URL = "/modsocket?player=" + Minecraft.getMinecraft().getSession().getUsername() + "&version=1.5.5-Alpha&SId=" + getSessionID();
        System.out.println("wss://sky.coflnet.com" + URL);
        try {
            setWebsocketClient(new URI("wss://sky.coflnet.com" + URL), this::onOpen, this::onMessage, this::onClose, this::onError);
            websocketClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getSessionID() {
        if (FlipConfig.SId.isEmpty() || FlipConfig.sessionExpiresIn.isEmpty() || Long.parseLong(FlipConfig.sessionExpiresIn) <= System.currentTimeMillis()) {
            FlipConfig.SId = UUID.randomUUID().toString();
            FlipConfig.sessionExpiresIn = "" + (System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 180);
        }

        return FlipConfig.SId;
    }

    private static IChatComponent handleMessage(JsonObject data) {
        String text = data.get("text").toString().replace("\"", "");
        String onClick = data.get("onClick").toString().replace("\"", "");
        String hover = data.get("hover").toString().replace("\"", "");
        ChatStyle style = new ChatStyle();

        if (!onClick.equals("null")) {
            if (onClick.startsWith("http")) {
                style.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, onClick));
            } else {
                style.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, onClick));
            }
        }
        if (!hover.equals("null")) {
            style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(hover)));
        }
        return new ChatComponentText("§7" + text).setChatStyle(style);
    }

    public void entityJoinWorldEvent(EntityJoinWorldEvent event) {
        if (!connected && Main.mc.thePlayer != null) {
            reconnect();
            connected = true;
        }
    }
}
