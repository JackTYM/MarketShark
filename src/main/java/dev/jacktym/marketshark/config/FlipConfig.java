package dev.jacktym.marketshark.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.config.data.OptionSize;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.jacktym.marketshark.util.DiscordIntegration;
import net.minecraft.client.Minecraft;

public class FlipConfig extends Config {

    @Checkbox(
            category = "Macros",
            subcategory = "Auto Open",
            name = "Auto Open",
            description = "Automatically opens the Auction GUI for new Cofl flips"
    )
    public static boolean autoOpen = false;
    @Checkbox(
            category = "Macros",
            subcategory = "Auto Buy",
            name = "Auto Buy",
            description = "Automatically buys opened Cofl flips",
            size = OptionSize.DUAL
    )
    public static boolean autoBuy = false;
    @Number(
            category = "Macros",
            subcategory = "Auto Buy",
            name = "Max Buy Clicks",
            description = "The max amount of times for the macro to click the Buy button (Default 2)",
            min = 1, max = 3
    )
    public static int maxBuyClicks = 2;
    //#if >=Megalodon
    @Checkbox(
            category = "Macros",
            subcategory = "Auto Buy",
            name = "Confirm Skip",
            description = "Skips the AutoBuy Confirm Window",
            size = OptionSize.DUAL
    )
    public static boolean confirmSkip = false;
    @Slider(
            category = "Macros",
            subcategory = "Auto Buy",
            name = "Confirm Skip Delay",
            description = "Delay to Skips the AutoBuy Confirm Window (Default 10)",
            min = 0, max = 30
    )
    public static int confirmSkipDelay = 2;
    //#endif >=Megalodon
    @Slider(
            category = "Macros",
            subcategory = "Auto Buy",
            name = "Bed Spam Delay",
            description = "How many ms to wait between bed buy clicks (Default 50)",
            min = 0, max = 100
    )
    public static int bedSpamDelay = 50;
    @Slider(
            category = "Macros",
            subcategory = "Auto Buy",
            name = "Bed Spam Start Delay",
            description = "How long to start spamming since start of the auction in ms (Default 19900)",
            min = 0, max = 30000
    )
    public static int bedSpamStartDelay = 19900;
    @Checkbox(
            category = "Macros",
            subcategory = "Auto Claim",
            name = "Auto Claim",
            description = "Auto Claims all Cofl Relay Flip Items"
    )
    public static boolean autoClaim = false;
    @Checkbox(
            category = "Macros",
            subcategory = "Auto Sell",
            name = "Auto Sell",
            description = "Auto Sell all Cofl Relay Flip Items",
            size = OptionSize.DUAL
    )
    public static boolean autoSell = false;
    @Number(
            category = "Macros",
            subcategory = "Auto Sell",
            name = "Auto Sell Time",
            description = "How many time in hours to list items for (Default 48)",
            min = 1, max = 168
    )
    public static int autoSellTime = 48;
    @Dropdown(
            category = "Macros",
            subcategory = "Auto Sell",
            name = "Auto Sell Price",
            description = "How to base your sell price",
            options = {
                    "Cofl LBin",
                    "Cofl LBin - 5 Percent",
                    "Cofl Median",
                    "Cofl Median - 5 Percent",
                    "Flip Target"
            }
    )
    public static int autoSellPrice = 4;
    @Checkbox(
            category = "Macros",
            subcategory = "Auto Claim Sold",
            name = "Auto Claim Sold",
            description = "Auto Claims all Sold Auctions"
    )
    public static boolean autoClaimSold = false;
    //#if >=Wobbegong
    @Dropdown(
            category = "Macros",
            subcategory = "Auto Relist",
            name = "Auto Relist",
            description = "Auto Relists Expired Auctions",
            options = {
                    "Disabled",
                    "Same Price",
                    "New Value"
            }
    )
    public static int autoRelist = 0;
    //#endif >=Wobbegong

    @Number(
            category = "Failsafes",
            name = "Stuck Menu Delay",
            description = "How many MS before auto closing a gui (Default 15000)",
            min = 0, max = 45000
    )
    public static int autoCloseMenuDelay = 15000;
    @Checkbox(
            category = "Failsafes",
            name = "Enable Maximum Item List Cost",
            description = "Enables cancelling listing an item if over a specific price"
    )
    public static boolean enableMaxList = false;
    @Number(
            category = "Failsafes",
            name = "Maximum Item List Cost",
            description = "The maximum amount to auto list an item for (Default 1000000000)",
            min = 0, max = 1000000000000f
    )
    public static int maximumAutoList = 1000000000;
    @Checkbox(
            category = "Failsafes",
            subcategory = "Minimum Profit Percent",
            name = "Enable Minimum Profit Percent",
            description = "Enables skipping listing an item if under x% profit"
    )
    public static boolean enableMinProfitPercent = false;
    @Number(
            category = "Failsafes",
            subcategory = "Minimum Profit Percent",
            name = "Minimum Profit Percent",
            description = "The minimum profit percent to auto list an item for (Default 3)",
            min = 0, max = 100
    )
    public static int minimumProfitPercent = 3;
    @Checkbox(
            category = "Failsafes",
            name = "Anti Limbo",
            description = "Brings you back to your Skyblock Island if sent to Limbo"
    )
    public static boolean antiLimbo = false;
    @Checkbox(
            category = "Failsafes",
            name = "Auto Reconnect",
            description = "Reconnects to Hypixel when kicked (if macro enabled)"
    )
    public static boolean autoReconnect = false;
    @Checkbox(
            category = "Failsafes",
            name = "Auto Island",
            description = "Reconnects to Island when closed"
    )
    public static boolean autoIsland = false;


    @Checkbox(
            category = "Webhooks",
            name = "Flip Bought",
            description = "Webhook whenever a flip is purchased by the macro"
    )
    public static boolean boughtWebhooks = false;
    @Checkbox(
            category = "Webhooks",
            name = "Flip Listed",
            description = "Webhook whenever a flip is listed by the macro"
    )
    public static boolean listedWebhooks = false;
    @Checkbox(
            category = "Webhooks",
            name = "Flip Sold",
            description = "Webhook whenever a flip is sold by the macro"
    )
    public static boolean soldWebhooks = false;


    @Checkbox(
            category = "Flip Finders",
            name = "Whitelist Flipper",
            description = "Enables AutoSell for the Flipper finder"
    )
    public static boolean flipper = true;
    @Checkbox(
            category = "Flip Finders",
            name = "Whitelist Sniper",
            description = "Enables AutoSell for the Sniper finder"
    )
    public static boolean sniper = true;
    @Checkbox(
            category = "Flip Finders",
            name = "Whitelist Sniper (Median)",
            description = "Enables AutoSell for the Sniper (Median) finder"
    )
    public static boolean sniperMedian = true;
    @Checkbox(
            category = "Flip Finders",
            name = "Whitelist User",
            description = "Enables AutoSell for the User finder"
    )
    public static boolean user = false;
    @Checkbox(
            category = "Flip Finders",
            name = "Whitelist TFM",
            description = "Enables AutoSell for the TFM finder"
    )
    public static boolean tfm = false;
    @Checkbox(
            category = "Flip Finders",
            name = "Whitelist Stonks",
            description = "Enables AutoSell for the Stonks finder"
    )
    public static boolean stonks = true;


    @Checkbox(
            category = "Developer",
            name = "Debug Mode",
            description = "Prints Debugging Messages"
    )
    public static boolean debug = false;
    @Checkbox(
            category = "Developer",
            name = "Anti Render mode",
            description = "Stops rendering all chunks for higher FPS"
    )
    public static boolean antiRender = false;
    @Text(
            category = "Developer",
            name = "Activation Key",
            description = "Paste your Discord Activation Key here!",
            secure = true
    )
    public static String activationKey = "";
    @Text(
            category = "Developer",
            name = "Last Updated",
            description = "Dont touch this if you dont want to break config syncing!",
            secure = true
    )
    public static String lastUpdated = "0";
    @Text(
            category = "Developer",
            name = "Config ID",
            description = "Dont touch this if you dont want to break config syncing!",
            secure = true
    )
    public static String configId = "DEFAULT";
    @Button(
            category = "Developer",
            name = "Connect to Discord Integration",
            text = "Connect!"
    )
    Runnable connectToDiscordIntegration = DiscordIntegration::connectToWebsocket;

    public FlipConfig() {
        super(new Mod("MarketShark", ModType.SKYBLOCK), "marketshark.toml");
        initialize();

        Runnable reloadChunks = () -> Minecraft.getMinecraft().renderGlobal.loadRenderers();
        addListener("antiRender", reloadChunks);

        Runnable syncConfig = () -> {
            lastUpdated = "" + System.currentTimeMillis();
            syncConfig();
        };
        addListener("", syncConfig);

        addListener("maxBuyClicks", syncConfig);
        addListener("bedSpamDelay", syncConfig);
        addListener("bedSpamStartDelay", syncConfig);
        addListener("autoSellTime", syncConfig);
        addListener("autoSellPrice", syncConfig);
        addListener("autoCloseMenuDelay", syncConfig);
        addListener("enableMaxList", syncConfig);
        addListener("maximumAutoList", syncConfig);
        addListener("enableMinProfitPercent", syncConfig);
        addListener("minimumProfitPercent", syncConfig);
        addListener("antiLimbo", syncConfig);
        addListener("autoReconnect", syncConfig);
        addListener("autoIsland", syncConfig);
        addListener("boughtWebhooks", syncConfig);
        addListener("listedWebhooks", syncConfig);
        addListener("soldWebhooks", syncConfig);
        addListener("flipper", syncConfig);
        addListener("sniper", syncConfig);
        addListener("sniperMedian", syncConfig);
        addListener("user", syncConfig);
        addListener("tfm", syncConfig);
        addListener("stonks", syncConfig);
    }

    public static JsonObject serialize() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("autoOpen", autoOpen);

        jsonObject.addProperty("autoBuy", autoBuy);
        jsonObject.addProperty("maxBuyClicks", maxBuyClicks);
        jsonObject.addProperty("bedSpamDelay", bedSpamDelay);
        jsonObject.addProperty("bedSpamStartDelay", bedSpamStartDelay);
        jsonObject.addProperty("autoClaim", autoClaim);
        jsonObject.addProperty("autoSell", autoSell);
        jsonObject.addProperty("autoSellTime", autoSellTime);
        jsonObject.addProperty("autoSellPrice", autoSellPrice);
        jsonObject.addProperty("autoClaimSold", autoClaimSold);
        jsonObject.addProperty("autoRelist", autoRelist);
        jsonObject.addProperty("autoCloseMenuDelay", autoCloseMenuDelay);
        jsonObject.addProperty("enableMaxList", enableMaxList);
        jsonObject.addProperty("maximumAutoList", maximumAutoList);
        jsonObject.addProperty("enableMinProfitPercent", enableMinProfitPercent);
        jsonObject.addProperty("minimumProfitPercent", minimumProfitPercent);
        jsonObject.addProperty("antiLimbo", antiLimbo);
        jsonObject.addProperty("autoReconnect", autoReconnect);
        jsonObject.addProperty("autoIsland", autoIsland);
        jsonObject.addProperty("boughtWebhooks", boughtWebhooks);
        jsonObject.addProperty("listedWebhooks", listedWebhooks);
        jsonObject.addProperty("soldWebhooks", soldWebhooks);
        jsonObject.addProperty("flipper", flipper);
        jsonObject.addProperty("sniper", sniper);
        jsonObject.addProperty("sniperMedian", sniperMedian);
        jsonObject.addProperty("user", user);
        jsonObject.addProperty("tfm", tfm);
        jsonObject.addProperty("stonks", stonks);
        return jsonObject;
    }

    public static void load(String config, String configId) {
        JsonObject jsonObject = new JsonParser().parse(config).getAsJsonObject();

        autoOpen = jsonObject.get("autoOpen").getAsBoolean();
        autoBuy = jsonObject.get("autoBuy").getAsBoolean();
        maxBuyClicks = jsonObject.get("maxBuyClicks").getAsInt();
        bedSpamDelay = jsonObject.get("bedSpamDelay").getAsInt();
        bedSpamStartDelay = jsonObject.get("bedSpamStartDelay").getAsInt();
        autoClaim = jsonObject.get("autoClaim").getAsBoolean();
        autoSell = jsonObject.get("autoSell").getAsBoolean();
        autoSellTime = jsonObject.get("autoSellTime").getAsInt();
        autoSellPrice = jsonObject.get("autoSellPrice").getAsInt();
        autoClaimSold = jsonObject.get("autoClaimSold").getAsBoolean();
        autoRelist = jsonObject.get("autoRelist").getAsInt();
        autoCloseMenuDelay = jsonObject.get("autoCloseMenuDelay").getAsInt();
        enableMaxList = jsonObject.get("enableMaxList").getAsBoolean();
        maximumAutoList = jsonObject.get("maximumAutoList").getAsInt();
        enableMinProfitPercent = jsonObject.get("enableMinProfitPercent").getAsBoolean();
        minimumProfitPercent = jsonObject.get("minimumProfitPercent").getAsInt();
        antiLimbo = jsonObject.get("antiLimbo").getAsBoolean();
        autoReconnect = jsonObject.get("autoReconnect").getAsBoolean();
        autoIsland = jsonObject.get("autoIsland").getAsBoolean();
        boughtWebhooks = jsonObject.get("boughtWebhooks").getAsBoolean();
        listedWebhooks = jsonObject.get("listedWebhooks").getAsBoolean();
        soldWebhooks = jsonObject.get("soldWebhooks").getAsBoolean();
        flipper = jsonObject.get("flipper").getAsBoolean();
        sniper = jsonObject.get("sniper").getAsBoolean();
        sniperMedian = jsonObject.get("sniperMedian").getAsBoolean();
        user = jsonObject.get("user").getAsBoolean();
        tfm = jsonObject.get("tfm").getAsBoolean();
        stonks = jsonObject.get("stonks").getAsBoolean();

        FlipConfig.configId = configId;
    }

    public static void syncConfig() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("config", serialize().toString());
        jsonObject.addProperty("lastUpdated", lastUpdated);

        DiscordIntegration.sendToWebsocket("SyncConfig", jsonObject.toString());
    }
}
