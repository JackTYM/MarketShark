package dev.jacktym.coflflip.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import dev.jacktym.coflflip.config.FlipConfig;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.Map;

public class DiscordIntegration {
    public static WebSocket ws;
    public static boolean connected = false;
    public static void connectToWebsocket() {
        try {
            if (ws != null) {
                ws.disconnect();
                connected = false;
            }
            ws = new WebSocketFactory().createSocket("wss://cofl.jacktym.dev");
            ws.addListener(new WebSocketAdapter() {
                @Override
                public void onTextMessage(WebSocket websocket, String message) throws Exception {
                    System.out.println("WebSocket Message: " + message);
                    JsonObject jsonObject = new JsonParser().parse(message).getAsJsonObject();

                    switch (jsonObject.get("type").getAsString()) {
                        case "Activated": {
                            ChatUtils.printMarkedChat("Successfully activated with Discord!");
                            System.out.println("Successfully activated with Discord!");
                            break;
                        }
                        case "FailedActivation": {
                            System.out.println("Failed activation. Please check your activation key!");
                            Minecraft.getMinecraft().shutdown();
                            break;
                        }
                    }
                }

                @Override
                public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                    super.onConnected(websocket, headers);
                    sendToWebsocket("Activating", "");
                    connected = true;
                }

                @Override
                public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                    super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
                    connected = false;
                    System.out.println("Disconnected from Discord Integration! Attempting to Reconnect in 15 seconds!");
                    ChatUtils.printMarkedChat("Disconnected from Discord Integration! Attempting to Reconnect in 15 seconds!");
                    DelayUtils.delayAction(15000, () -> {
                        if (!connected) {
                            connectToWebsocket();
                        }
                    });
                }
            });
            ws.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendToWebsocket(String type, String message) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", type);
        jsonObject.addProperty("message", message);
        jsonObject.addProperty("key", FlipConfig.activationKey);
        System.out.println("Sending " + jsonObject);
        ws.sendText(jsonObject.toString());
    }
}
