package dev.jacktym.marketshark.util;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.google.gson.*;
import dev.jacktym.marketshark.Main;
import dev.jacktym.marketshark.config.FlipConfig;
import dev.jacktym.marketshark.macros.AutoClaimSold;
import dev.jacktym.marketshark.macros.AutoList;
import dev.jacktym.marketshark.mixins.GuiNewChatAccessor;
import net.minecraft.client.Minecraft;
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
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

public class DiscordIntegration {
    private static String sessionId = null;
    public static WebSocketClient websocketClient;
    public static boolean connected = false;

    private static boolean statsSent = true;

    private static String purse = "";

    private static String island = "";

    private static String visitors = "";

    private static String hypixelPing = "";

    private static String coflPing = "";
    private static String coflDelay = "";
    public static boolean activated = false;

    public static void connectToWebsocket() {
        try {
            setWebsocketClient(new URI("wss://wss.$DOMAIN"));
            connected = true;
            websocketClient.connect();
        } catch (Exception e) {
            BugLogger.logError(e);
        }
    }

    public static void setWebsocketClient(URI serverUri) {
        if (websocketClient != null && websocketClient.isOpen()) {
            try {
                websocketClient.closeBlocking();
            } catch (Exception e) {
                BugLogger.logError(e);
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
                    GuiUtil.singleClick(15);
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
            if (chest.getStackInSlot(18) == null) {
                return false;
            }
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(FlipItem.class, (JsonSerializer<FlipItem>) (src, typeOfSrc, context) -> src.serialize());
            Gson gson = gsonBuilder.create();

            List<FlipItem> items = new ArrayList<>();
            System.out.println(ChatUtils.stripColor(chest.getDisplayName().getUnformattedText()));
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

                        FlipItem item = AutoClaimSold.getItemFromAuction(stack);

                        items.add(item);
                    }
                }

                System.out.println(1);
                // Claim sold flips
                if (chest.getStackInSlot(30) != null && chest.getStackInSlot(30).getDisplayName().contains("Claim All")) {

                    System.out.println(2);
                    Main.mc.playerController.windowClick(Main.mc.thePlayer.openContainer.windowId, 30, 2, 3, Main.mc.thePlayer);
                } else if (chest.getStackInSlot(21) != null && chest.getStackInSlot(21).getDisplayName().contains("Claim All")) {

                    System.out.println(3);
                    Main.mc.playerController.windowClick(Main.mc.thePlayer.openContainer.windowId, 21, 2, 3, Main.mc.thePlayer);
                } else {
                    System.out.println(4);
                    Main.mc.thePlayer.closeScreen();
                }
                System.out.println(5);

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
            if (coflPing.contains("\\n")) {
                coflPing = coflPing.split("\\n")[0];
            }
            coflPing = coflPing.split("\\.")[0] + "." + coflPing.split("\\.")[1].charAt(0) + "ms";
            coflPing = coflPing.replace("msms", "ms");
            return true;
        }
        return false;
    }

    public static boolean getCoflDelay(String message) {

        message = ChatUtils.stripColor(message);
        if (message.contains("You are currently delayed by")) {
            coflDelay = message.split("a maximum of ")[1].split(" by the fairness system")[0];
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

            List<String> clickRows = new ArrayList<>();
            List<String> clickColumns = new ArrayList<>();

            int clickIndex = 0;
            StringBuilder captchaString = new StringBuilder();

            List<String> tempColumns = new ArrayList<>();
            for (JsonElement element : captcha) {

                String line = element.getAsJsonObject().get("text").getAsString().replace("\\n", "\n").replace("\uD83C\uDDE7", "").replace("\uD83C\uDDFE", "");
                captchaString.append(line);

                if (!element.getAsJsonObject().get("onClick").isJsonNull()) {
                    if (clickRows.size() < clickIndex) {
                        clickRows.add(element.getAsJsonObject().get("onClick").getAsString());
                    }

                    for (String character : line.split("")) {
                        int length = 0;

                        if (character.matches("\uFFFD")) {
                            length = 8;
                        } else if (character.equals("⋅")) {
                            length = 4;
                        } else if (character.equals(" ")) {
                            length = 8;
                        }

                        for (int i = 0; i < length; i++) {
                            if (!element.getAsJsonObject().get("onClick").getAsString().contains("config")) {
                                tempColumns.add(element.getAsJsonObject().get("onClick").getAsString());
                            }
                        }
                    }
                }
                if (element.getAsJsonObject().get("text").getAsString().contains("\\n")) {
                    clickIndex++;

                    if (tempColumns.size() > clickColumns.size()) {
                        clickColumns.clear();
                        clickColumns.addAll(tempColumns);
                    }
                    tempColumns = new ArrayList<>();
                }
            }

            System.out.println("Column Clicks Length " + clickColumns.size());

            HashMap<String, Integer> characters = new HashMap<>();

            for (char c : captchaString.toString().toCharArray()) {
                if (!characters.containsKey(String.valueOf(c))) {
                    characters.put(String.valueOf(c), Minecraft.getMinecraft().fontRendererObj.getCharWidth(c));
                }
            }

            JsonObject response = new JsonObject();
            response.addProperty("captcha", captchaString.toString());
            response.addProperty("onClicks", clickRows.toString());
            response.addProperty("clickColumns", clickColumns.toString());
            response.addProperty("lengths", characters.toString());

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
            stats.addProperty("purse", purse);
            stats.addProperty("island", island);
            stats.addProperty("visitors", visitors);
            stats.addProperty("status", FlipConfig.autoBuy);
            stats.addProperty("hypixel_ping", hypixelPing);
            stats.addProperty("cofl_ping", coflPing);
            stats.addProperty("cofl_delay", coflDelay);
            stats.addProperty("paused", Main.paused);
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

    private static List<Map.Entry<String, String>> websocketQueue = new ArrayList<>();

    public static void sendToWebsocket(String type, String message) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", type);
        jsonObject.addProperty("message", message);

        System.out.println("Sending " + jsonObject);
        sendNoLog(type, message);
    }

    public static void sendNoLog(String type, String message) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", type);
        jsonObject.addProperty("message", message);

        jsonObject.addProperty("key", FlipConfig.activationKey);
        jsonObject.addProperty("username", Main.mc.getSession().getUsername());
        jsonObject.addProperty("hwid", getHWID());
        jsonObject.addProperty("version", Main.version);
        jsonObject.addProperty("modVersion", Main.modVersion);
        if (sessionId != null) {
            jsonObject.addProperty("session_id", sessionId);
        }

        if (websocketClient.isOpen()) {
            try {
                websocketClient.send(jsonObject.toString());
            } catch (Exception e) {
                BugLogger.logError(e);
            }
        } else {
            BugLogger.log("Client closed. Added to queue!", FlipConfig.debug);
            websocketQueue.add(new AbstractMap.SimpleEntry<>(type, message));
        }
    }

    public static String getHWID() {
        try {
            String toEncrypt = System.getenv("COMPUTERNAME") + System.getProperty("user.name") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_LEVEL");
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(toEncrypt.getBytes());
            StringBuffer hexString = new StringBuffer();

            byte byteData[] = md.digest();

            for (byte aByteData : byteData) {
                String hex = Integer.toHexString(0xff & aByteData);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            BugLogger.logError(e);
            return "Error";
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

        List<Map.Entry<String, String>> tempQueue = new ArrayList<>(websocketQueue);
        for (Map.Entry<String, String> message : tempQueue) {
            System.out.println("Running " + message.getKey() + " From Queue!");
            websocketQueue.remove(message);
            sendToWebsocket(message.getKey(), message.getValue());
        }
    }


    public static void onMessage(String message) {
        System.out.println("WebSocket Message: " + message);
        JsonObject jsonObject = new JsonParser().parse(message).getAsJsonObject();

        if (jsonObject.has("username")) {
            String username = jsonObject.get("username").getAsString();
            if (!username.isEmpty()) {
                if (!Main.mc.getSession().getUsername().equals(username)) {
                    BugLogger.log("WebSocket Message ignored! For different username", FlipConfig.debug);
                    return;
                }
            }
        }

        switch (jsonObject.get("type").getAsString()) {
            case "Activated": {
                sessionId = jsonObject.get("session_id").getAsString();
                BugLogger.logChat(jsonObject.get("message").getAsString(), true);

                FlipConfig.syncConfig();

                activated = true;
                break;
            }
            case "Reconnected": {
                BugLogger.logChat(jsonObject.get("message").getAsString(), true);

                FlipConfig.syncConfig();

                activated = true;
                break;
            }
            case "FailedActivation": {
                activated = false;
                BugLogger.logChat(jsonObject.get("message").getAsString(), true);
                break;
            }
            case "IncorrectSession": {
                System.out.println("Incorrect Session!");
                sessionId = null;
                websocketClient.close();
                connectToWebsocket();
                activated = false;
                break;
            }

            //#if >=GreatWhite
            case "Stats": {
                BugLogger.logChat(jsonObject.get("message").getAsString(), true);

                purse = "Unknown";
                island = "Unknown";
                visitors = "Unknown";
                hypixelPing = "Unknown";
                coflPing = "Unknown";
                coflDelay = "Unknown";
                statsSent = false;

                if (Main.mc != null && Main.mc.thePlayer != null) {
                    Main.mc.thePlayer.sendQueue.addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.REQUEST_STATS));
                    long sendPing = System.currentTimeMillis();
                    RealtimeEventRegistry.registerPacket(packet -> getPingPacket(packet, sendPing), "DiscordIntegration");

                    RealtimeEventRegistry.registerMessage("coflMessage", DiscordIntegration::getCoflPing, "DiscordIntegration");
                    ClientCommandHandler.instance.executeCommand(Main.mc.thePlayer, "/cofl ping");
                    RealtimeEventRegistry.registerMessage("coflMessage", DiscordIntegration::getCoflDelay, "DiscordIntegration");
                    ClientCommandHandler.instance.executeCommand(Main.mc.thePlayer, "/cofl delay");

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
                    FlipConfig.autoSellTime = settings.get("autoSellTime").getAsInt();
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

                Main.flipConfig.save();

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
                                        BugLogger.logChat("No Cofl Flip to use. Defaulting to Median Price", true);
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

            case "Captcha": {
                ClientCommandHandler.instance.executeCommand(Main.mc.thePlayer, "/cofl captcha vertical");
                break;
            }

            case "HorizontalCaptcha": {
                ClientCommandHandler.instance.executeCommand(Main.mc.thePlayer, "/cofl captcha optifine");
                break;
            }

            case "CaptchaSolve": {
                ClientCommandHandler.instance.executeCommand(Main.mc.thePlayer, jsonObject.get("message").getAsString());
                break;
            }
            //#endif >=GreatWhite
            case "Pause": {
                Main.paused = true;
                Reset();
                break;
            }

            case "Unpause": {
                Main.paused = false;
                break;
            }

            case "BugLog": {
                BugLogger.sendBugLog();
                break;
            }

            case "ConfigSync": {
                BugLogger.logChat("Synced config with server! Loading config id " + jsonObject.get("configId").getAsString() + ". To revert to old config, run /ms load " + jsonObject.get("oldConfigId").getAsString(), true);

                FlipConfig.load(jsonObject.get("config").getAsString(), jsonObject.get("configId").getAsString());

                break;
            }

            case "ConfigLoadMissing": {
                BugLogger.logChat("Failed to load config by ID. Double check the config ID you are using!", true);

                break;
            }
        }
    }

    public static void Reset() {
        RealtimeEventRegistry.eventMap.clear();
        RealtimeEventRegistry.classMap.clear();
        RealtimeEventRegistry.eventMap.clear();
        RealtimeEventRegistry.packetClassMap.clear();
        AutoList.listingInv = false;
        AutoList.finishCurrentListing();
        QueueUtil.queue.clear();
        QueueUtil.finishAction();
        DelayUtils.resetTimer();
    }

    static TimerTask reconnectTimer;

    public static void onClose(int code, String reason, boolean remote) {
        if (activated) {
            connected = false;

            System.out.println("Disconnected from Discord Integration!");
            System.out.println("Websocket closed with reason: " + reason + " and code " + code + " Remote? " + remote);

            // 1006 = Cloudflare Restart
            if (remote && code != 1006) {
                BugLogger.logChat("Disconnected from Discord Integration! Attempting to Reconnect in 5 seconds!", true);
                reconnect();
            } else {
                reconnect();
            }
        }
    }


    public static void onError(Exception ex) {
        connected = false;

        ex.printStackTrace();

        System.out.println("Disconnected from Discord Integration!");
        System.out.println("Websocket closed with reason: " + ex);
        BugLogger.logChat("Disconnected from Discord Integration! Attempting to Reconnect in 5 seconds!", true);

        reconnect();
    }

    public static void reconnect() {
        if (reconnectTimer == null) {
            reconnectTimer = DelayUtils.delayAction(5000, () -> {
                connectToWebsocket();
                reconnectTimer = null;
            });
        }
    }
}
