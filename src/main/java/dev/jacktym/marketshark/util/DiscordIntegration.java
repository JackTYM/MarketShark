package dev.jacktym.marketshark.util;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.google.gson.*;
import dev.jacktym.marketshark.Main;
import dev.jacktym.marketshark.config.FlipConfig;
import dev.jacktym.marketshark.macros.AutoClaimSold;
import dev.jacktym.marketshark.mixins.GuiNewChatAccessor;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.server.S37PacketStatistics;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DiscordIntegration {
    private static String sessionId = null;
    public static WebSocketClient websocketClient;
    public static boolean connected = false;

    private static boolean statsSent = true;
    private static String unsold = "";

    private static String purse = "";

    private static String island = "";

    private static String visitors = "";

    private static String hypixelPing = "";

    private static String coflPing = "";

    public static void connectToWebsocket() {
        try {
            setwebsocketClient(new URI("wss://cofl.jacktym.dev"));
            connected = true;
            websocketClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setwebsocketClient(URI serverUri) {
        if (websocketClient != null && websocketClient.isOpen()) {
            try {
                websocketClient.closeBlocking();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        websocketClient = new WebSocketClient(serverUri) {
            @Override
            public void onOpen(ServerHandshake handshakeData) {
                DiscordIntegration.onOpen(handshakeData);
            }

            @Override
            public void onMessage(String message) {
                DiscordIntegration.onMessage(message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                DiscordIntegration.onClose(code, reason, remote);
            }

            @Override
            public void onError(Exception ex) {
                DiscordIntegration.onError(ex);
            }
        };

        websocketClient.setConnectionLostTimeout(0);
    }

    //#if >=GreatWhite
    public static boolean getOwnedAuctions(GuiScreenEvent event) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiChest) {
            IInventory chest = GuiUtil.getInventory(event.gui);
            if (chest.getStackInSlot(15) == null) {
                return false;
            }

            if (chest.getDisplayName().getUnformattedText().endsWith("Auction House")) {
                String auctionAmount = ChatUtils.stripColor(chest.getStackInSlot(15).getTagCompound().getCompoundTag("display").getTagList("Lore", 8).getStringTagAt(0));

                if (auctionAmount.contains("You own ")) {
                    unsold = auctionAmount.split("You own ")[1].split(" auction")[0];

                    Main.mc.thePlayer.closeScreen();
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean openManageAuctions(GuiScreenEvent event) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiChest) {
            IInventory chest = GuiUtil.getInventory(event.gui);
            if (chest.getStackInSlot(0) == null) {
                return false;
            }

            if (chest.getDisplayName().getUnformattedText().endsWith("Auction House")) {
                DelayUtils.delayAction(800, () -> {
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> sendAuctions((GuiScreenEvent) guiScreenEvent), "AutoList");
                    GuiUtil.tryClick(15);
                });
                return true;
            }
        }
        return false;
    }

    public static boolean sendAuctions(GuiScreenEvent event) {
        if (!(event instanceof GuiScreenEvent.DrawScreenEvent.Post)) {
            // GUI not initialized yet
            return false;
        }
        if (event.gui instanceof GuiChest) {
            IInventory chest = GuiUtil.getInventory(event.gui);
            if (chest.getStackInSlot(0) == null) {
                return false;
            }
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(FlipItem.class, (JsonSerializer<FlipItem>) (src, typeOfSrc, context) -> src.serialize());
            Gson gson = gsonBuilder.create();

            List<FlipItem> items = new ArrayList<>();
            System.out.println(ChatUtils.stripColor(chest.getDisplayName().getUnformattedText()));
            if (ChatUtils.stripColor(chest.getDisplayName().getUnformattedText()).equals("Manage Auctions")) {
                for (int i = 1; i <= 14; i++) {
                    int slotId = i + 9;
                    if (i > 7) slotId += 2;

                    ItemStack stack = chest.getStackInSlot(slotId);
                    if (stack != null) {
                        if (stack.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane)) {
                            break;
                        }

                        FlipItem item = AutoClaimSold.getItemFromAuction(stack);

                        items.add(item);
                    }
                }
                Main.mc.thePlayer.closeScreen();

                JsonObject response = new JsonObject();
                response.addProperty("items", gson.toJson(items));

                DiscordIntegration.sendToWebsocket("AuctionHouse", response.toString());
                RealtimeEventRegistry.clearClazzMap("DiscordIntegration");
                
                return true;
            } else if (ChatUtils.stripColor(chest.getDisplayName().getUnformattedText()).contains("Create")) {
                Main.mc.thePlayer.closeScreen();

                JsonObject response = new JsonObject();
                response.addProperty("items", "None");

                DiscordIntegration.sendToWebsocket("AuctionHouse", response.toString());
                RealtimeEventRegistry.clearClazzMap("DiscordIntegration");
                
                return true;
            }
        }
        return false;
    }

    public static boolean getPingPacket(Packet packet, long sendTime) {
        if (packet instanceof S37PacketStatistics) {
            hypixelPing = "" + (System.currentTimeMillis() - sendTime) + "ms";
            return true;
        }
        return false;
    }

    public static boolean getCoflPing(String message) {

        System.out.println("BLA BLA BLA " + ChatUtils.stripColor(message));

        message = ChatUtils.stripColor(message);
        if (message.contains("Your Ping")) {
            coflPing = message.split("is: ")[1];
            coflPing = coflPing.split("\\.")[0] + "." + coflPing.split("\\.")[1].charAt(0) + "ms";
            return true;
        }
        return false;
    }
    //#endif >=GreatWhite

    public static void getCoflCaptcha(String message) {
        JsonObject jsonObject = new JsonParser().parse(message).getAsJsonObject();
        String dataStr = jsonObject.get("data").toString();
        String strippedData = dataStr
                .substring(1, dataStr.length() - 1)
                .replace("\\\"", "\"");

        strippedData = ChatUtils.stripColor(strippedData);
        //#if Hammerhead
        if (strippedData.contains("/cofl captcha ")) {
            JsonObject response = new JsonObject();
            response.addProperty("message", strippedData);
            

            DiscordIntegration.sendToWebsocket("Captcha", response.toString());
        }
        //#endif Hammerhead

        //#if >=GreatWhite
        if (strippedData.contains("Click to get a letter captcha to prove you are not.") && !strippedData.contains("You are currently delayed for likely being afk")) {
            ClientCommandHandler.instance.executeCommand(Main.mc.thePlayer, "/cofl captcha vertical");
        } else if (strippedData.contains("/cofl captcha ")) {
            JsonArray captcha = new JsonParser().parse(strippedData).getAsJsonArray();

            List<String> captchaClicks = new ArrayList<>();
            int clickIndex = 0;
            StringBuilder captchaString = new StringBuilder();
            for (JsonElement element : captcha) {
                captchaString.append(element.getAsJsonObject().get("text").getAsString().replace("\\n", "\n").replace("\uD83C\uDDE7", "").replace("\uD83C\uDDFE", ""));

                if (!element.getAsJsonObject().get("onClick").isJsonNull()) {
                    if (captchaClicks.size() < clickIndex) {
                        captchaClicks.add(element.getAsJsonObject().get("onClick").getAsString());
                    }
                }
                if (element.getAsJsonObject().get("text").getAsString().contains("\\n")) {
                    clickIndex++;
                }
            }

            JsonObject response = new JsonObject();
            response.addProperty("captcha", captchaString.toString());
            response.addProperty("onClicks", captchaClicks.toString());
            

            DiscordIntegration.sendToWebsocket("Captcha", response.toString());
        } else if (strippedData.contains("Thanks for confirming that you are a real user")) {
            JsonObject response = new JsonObject();
            

            DiscordIntegration.sendToWebsocket("CaptchaSuccess", response.toString());
        } else if (strippedData.contains("You solved the captcha, but you failed too many previously")) {
            JsonObject response = new JsonObject();
            

            DiscordIntegration.sendToWebsocket("CaptchaCorrect", response.toString());
        } else if (strippedData.contains("Your answer was not correct")) {
            JsonObject response = new JsonObject();
            

            DiscordIntegration.sendToWebsocket("CaptchaIncorrect", response.toString());
        }
        //#endif >=GreatWhite
    }

    //#if >=GreatWhite
    public static void sendStats() {
        if (!statsSent) {
            statsSent = true;
            JsonObject stats = new JsonObject();
            stats.addProperty("unsold", unsold);
            stats.addProperty("purse", purse);
            stats.addProperty("island", island);
            stats.addProperty("visitors", visitors);
            stats.addProperty("status", FlipConfig.autoBuy);
            stats.addProperty("hypixel_ping", hypixelPing);
            stats.addProperty("cofl_ping", coflPing);
            DiscordIntegration.sendToWebsocket("Stats", stats.toString());

            if (Main.mc != null && Main.mc.thePlayer != null) {
                Main.mc.thePlayer.closeScreen();
            }
            RealtimeEventRegistry.clearClazzMap("DiscordIntegration");
            
        }
    }

    public static List<String> getScoreboard() {
        Scoreboard scoreboard = Main.mc.theWorld.getScoreboard();
        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        List<String> scoreList = scoreboard.getSortedScores(objective)
                .stream()
                .limit(15)
                .map(score ->
                        ScorePlayerTeam.formatPlayerName(
                                scoreboard.getPlayersTeam(score.getPlayerName()),
                                score.getPlayerName()))
                .map(line -> ChatUtils.stripColor(line).replaceAll("[^\\x00-\\x7F]", ""))
                .collect(Collectors.toList());
        Collections.reverse(scoreList);

        return scoreList;
    }
    //#endif >=GreatWhite

    public static void sendToWebsocket(String type, String message) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", type);
        jsonObject.addProperty("message", message);
        jsonObject.addProperty("key", FlipConfig.activationKey);
        jsonObject.addProperty("username", Main.mc.getSession().getUsername());
        if (sessionId != null) {
            jsonObject.addProperty("session_id", sessionId);
        }
        System.out.println("Sending " + jsonObject);
        if (websocketClient.isOpen()) {
            websocketClient.send(jsonObject.toString());
        } else {
            //connectToWebsocket();
            DelayUtils.delayAction(1000, () -> sendToWebsocket(type, message));
        }
    }


    public static void onOpen(ServerHandshake handshakedata) {
        System.out.println("Session ID " + sessionId);
        if (sessionId == null) {
            sendToWebsocket("Activating", "");
        } else {
            sendToWebsocket("Reconnecting", "");
        }
        connected = true;
    }


    public static void onMessage(String message) {
        System.out.println("WebSocket Message: " + message);
        JsonObject jsonObject = new JsonParser().parse(message).getAsJsonObject();

        switch (jsonObject.get("type").getAsString()) {
            case "Activated": {
                sessionId = jsonObject.get("session_id").getAsString();
                ChatUtils.printMarkedChat(jsonObject.get("message").getAsString());
                break;
            }
            case "FailedActivation": {
                Main.mc.shutdown();
                break;
            }
            case "IncorrectSession": {
                System.out.println("Incorrect Session!");
                sessionId = null;
                websocketClient.close();
                connectToWebsocket();
                break;
            }

            //#if >=GreatWhite
            case "Stats": {
                ChatUtils.printMarkedChat(jsonObject.get("message").getAsString());

                unsold = "0";
                purse = "Unknown";
                island = "Unknown";
                visitors = "Unknown";
                hypixelPing = "Unknown";
                coflPing = "Unknown";
                statsSent = false;

                if (Main.mc != null && Main.mc.thePlayer != null) {
                    QueueUtil.addToQueue(() -> {
                        Main.mc.thePlayer.sendChatMessage("/ah");
                        RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> getOwnedAuctions((GuiScreenEvent) guiScreenEvent), "DiscordIntegration");

                        Main.mc.thePlayer.sendQueue.addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.REQUEST_STATS));
                        long sendPing = System.currentTimeMillis();
                        RealtimeEventRegistry.registerPacket(packet -> getPingPacket(packet, sendPing), "DiscordIntegration");

                        RealtimeEventRegistry.registerMessage("coflMessage", DiscordIntegration::getCoflPing, "DiscordIntegration");
                        ClientCommandHandler.instance.executeCommand(Main.mc.thePlayer, "/cofl ping");

                        List<String> scoreboard = getScoreboard();
                        try {
                            purse = ChatUtils.stripColor(scoreboard.get(6)).split(": ")[1];
                        } catch (Exception ignored) {
                        }
                        try {
                            island = ChatUtils.stripColor(scoreboard.get(4));
                        } catch (Exception ignored) {
                        }
                        try {
                            List<String> tabList = new ArrayList<>();
                            List<NetworkPlayerInfo> list = new Ordering<NetworkPlayerInfo>() {
                                public int compare(NetworkPlayerInfo p_compare_1_, NetworkPlayerInfo p_compare_2_) {
                                    ScorePlayerTeam scoreplayerteam = p_compare_1_.getPlayerTeam();
                                    ScorePlayerTeam scoreplayerteam1 = p_compare_2_.getPlayerTeam();
                                    return ComparisonChain.start().compareTrueFirst(p_compare_1_.getGameType() != WorldSettings.GameType.SPECTATOR, p_compare_2_.getGameType() != WorldSettings.GameType.SPECTATOR).compare(scoreplayerteam != null ? scoreplayerteam.getRegisteredName() : "", scoreplayerteam1 != null ? scoreplayerteam1.getRegisteredName() : "").compare(p_compare_1_.getGameProfile().getName(), p_compare_2_.getGameProfile().getName()).result();
                                }
                            }.sortedCopy(Main.mc.thePlayer.sendQueue.getPlayerInfoMap());

                            for (NetworkPlayerInfo playerInfo : list) {
                                if (playerInfo.getDisplayName() != null) {
                                    tabList.add(ChatUtils.stripColor(playerInfo.getDisplayName().getUnformattedText()).replaceAll("[^\\x00-\\x7F]", ""));
                                }
                            }

                            visitors = tabList.get(20).split("\\(")[1].split("\\)")[0];

                        } catch (Exception ignored) {
                        }

                        DelayUtils.delayAction(5000, DiscordIntegration::sendStats);
                    });
                }

                break;
            }
            //#endif >=GreatWhite

            case "Settings": {
                JsonObject settings = new JsonParser().parse(jsonObject.get("message").getAsString()).getAsJsonObject();

                try {
                    FlipConfig.autoOpen = settings.get("autoOpen").getAsBoolean();
                } catch (Exception ignored) {
                }
                try {
                    FlipConfig.autoBuy = settings.get("autoBuy").getAsBoolean();
                } catch (Exception ignored) {
                }
                try {
                    FlipConfig.autoClaim = settings.get("autoClaim").getAsBoolean();
                } catch (Exception ignored) {
                }
                try {
                    FlipConfig.autoSell = settings.get("autoSell").getAsBoolean();
                } catch (Exception ignored) {
                }
                try {
                    FlipConfig.autoSellTime = settings.get("autoSellTime").getAsString();
                } catch (Exception ignored) {
                }
                try {
                    FlipConfig.autoSellPrice = settings.get("autoSellPrice").getAsInt();
                } catch (Exception ignored) {
                }
                try {
                    FlipConfig.autoClaimSold = settings.get("autoClaimSold").getAsBoolean();
                } catch (Exception ignored) {
                }

                JsonObject responseSettings = new JsonObject();
                responseSettings.addProperty("autoOpen", FlipConfig.autoOpen);
                responseSettings.addProperty("autoBuy", FlipConfig.autoBuy);
                responseSettings.addProperty("autoClaim", FlipConfig.autoClaim);
                responseSettings.addProperty("autoSell", FlipConfig.autoSell);
                responseSettings.addProperty("autoSellTime", FlipConfig.autoSellTime);
                responseSettings.addProperty("autoSellPrice", FlipConfig.autoSellPrice);
                responseSettings.addProperty("autoClaimSold", FlipConfig.autoClaimSold);

                DiscordIntegration.sendToWebsocket("Settings", responseSettings.toString());
                break;
            }

            //#if >=GreatWhite
            case "SendChat": {
                if (Main.mc != null && Main.mc.thePlayer != null) {
                    String send = jsonObject.get("message").getAsString();
                    if (send.startsWith("/")) {
                        // Allow client mods to pick up
                        ClientCommandHandler.instance.executeCommand(Main.mc.thePlayer, send);
                    } else {
                        Main.mc.thePlayer.sendChatMessage(send);
                    }

                    int chatLinesIndex = ((GuiNewChatAccessor) Main.mc.ingameGUI.getChatGUI()).getDrawnChatLines().size();

                    DelayUtils.delayAction(5000, () -> {
                        List<String> messages = new ArrayList<>();

                        List<ChatLine> chatLines = ((GuiNewChatAccessor) Main.mc.ingameGUI.getChatGUI()).getDrawnChatLines();

                        for (int i = chatLines.size() - chatLinesIndex - 1; i >= 0; i--) {
                            messages.add(ChatUtils.stripColor(chatLines.get(i).getChatComponent().getUnformattedText()));
                        }
                        JsonObject response = new JsonObject();
                        response.addProperty("chat", jsonObject.get("message").getAsString());
                        response.addProperty("messages", new Gson().toJson(messages));
                        

                        DiscordIntegration.sendToWebsocket("ChatResponses", response.toString());

                    });
                }
                break;
            }

            case "Chat": {
                if (Main.mc != null && Main.mc.thePlayer != null) {
                    List<String> messages = new ArrayList<>();

                    List<ChatLine> chatLines = ((GuiNewChatAccessor) Main.mc.ingameGUI.getChatGUI()).getDrawnChatLines();

                    // Get last x messages
                    for (int i = Math.min(chatLines.size() - 1, jsonObject.get("amount").getAsInt() - 1); i >= 0; i--) {
                        messages.add(ChatUtils.stripColor(chatLines.get(i).getChatComponent().getUnformattedText()));
                    }
                    JsonObject response = new JsonObject();
                    response.addProperty("messages", new Gson().toJson(messages));
                    

                    DiscordIntegration.sendToWebsocket("ChatMessages", response.toString());
                }
                break;
            }
            //#endif >=GreatWhite

            //#if >=Wobbegong
            case "Inventory": {
                if (Main.mc != null && Main.mc.thePlayer != null) {
                    ItemStack[] inventory = Main.mc.thePlayer.inventory.mainInventory;
                    JsonArray coflPrices = CoflAPIUtil.getCoflPrices(inventory);

                    List<FlipItem> items = new ArrayList<>();
                    for (int i = 0; i < inventory.length; i++) {
                        if (i == 8) {
                            // Skyblock Menu
                            continue;
                        }

                        if (inventory[i] != null) {
                            FlipItem item = null;
                            if (!FlipItem.getUuid(inventory[i]).isEmpty()) {
                                item = FlipItem.getFlipItem(inventory[i]);
                            }

                            if (item == null) {
                                item = FlipItem.getFlipItem(inventory[i]);
                            }

                            if (!coflPrices.get(i).isJsonNull()) {
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
                            }

                            items.add(item);
                        }
                    }
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.registerTypeAdapter(FlipItem.class, (JsonSerializer<FlipItem>) (src, typeOfSrc, context) -> src.serialize());
                    Gson gson = gsonBuilder.create();

                    JsonObject response = new JsonObject();
                    response.addProperty("items", gson.toJson(items));
                    

                    DiscordIntegration.sendToWebsocket("Inventory", response.toString());
                }
                break;
            }
            //#endif >=Wobbegong


            //#if >=GreatWhite
            case "AuctionHouse": {
                QueueUtil.addToQueue(() -> {
                    Main.mc.thePlayer.sendChatMessage("/ah");
                    RealtimeEventRegistry.registerEvent("guiScreenEvent", guiScreenEvent -> openManageAuctions((GuiScreenEvent) guiScreenEvent), "DiscordIntegration");

                    
                });
                break;
            }
            //#endif >=GreatWhite

            //#if >=GreatWhite
            case "Captcha": {
                ClientCommandHandler.instance.executeCommand(Main.mc.thePlayer, "/cofl captcha vertical");
                break;
            }

            case "CaptchaSolve": {
                ClientCommandHandler.instance.executeCommand(Main.mc.thePlayer, jsonObject.get("message").getAsString());
                break;
            }
            //#endif >=GreatWhite
        }
    }


    public static void onClose(int code, String reason, boolean remote) {
        connected = false;

        System.out.println("Disconnected from Discord Integration!");
        System.out.println("Websocket closed with reason: " + reason + " and code " + code + " Remote? " + remote);

        // 1006 = Cloudflare Restart
        if (remote && code != 1006) {
            ChatUtils.printMarkedChat("Disconnected from Discord Integration! Attempting to Reconnect in 5 seconds!");
            DelayUtils.delayAction(5000, () -> {
                if (!connected) {
                    connectToWebsocket();
                }
            });
        } else {
            connectToWebsocket();
        }
    }


    public static void onError(Exception ex) {
        connected = false;

        ex.printStackTrace();

        System.out.println("Disconnected from Discord Integration!");
        System.out.println("Websocket closed with reason: " + ex);
        ChatUtils.printMarkedChat("Disconnected from Discord Integration! Attempting to Reconnect in 5 seconds!");
        DelayUtils.delayAction(5000, () -> {
            if (!connected) {
                connectToWebsocket();
            }
        });
    }
}
