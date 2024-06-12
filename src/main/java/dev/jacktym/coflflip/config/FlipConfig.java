package dev.jacktym.coflflip.config;

import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class FlipConfig extends Vigilant {

    @Property(
            type = PropertyType.CHECKBOX,
            category = "Macros",
            name = "Auto Open",
            description = "Auto Opens all Cofl Relay Flips"
    )
    public static boolean autoOpen = false;
    @Property(
            type = PropertyType.CHECKBOX,
            category = "Macros",
            name = "Auto Buy",
            description = "Auto Buys Cofl Relay Flips"
    )
    public static boolean autoBuy = false;
    @Property(
            type = PropertyType.CHECKBOX,
            category = "Macros",
            name = "Confirm Skip",
            description = "Skips the AutoBuy Confirm Window"
    )
    public static boolean confirmSkip = false;
    @Property(
            type = PropertyType.TEXT,
            category = "Macros",
            name = "Confirm Skip Delay",
            description = "Delay to Skips the AutoBuy Confirm Window (Default 10)"
    )
    public static String confirmSkipDelay = "10";
    @Property(
            type = PropertyType.TEXT,
            category = "Macros",
            name = "Bed Spam Delay",
            description = "How many ms to wait between bed buy clicks (Default 50)"
    )
    public static String bedSpamDelay = "50";
    @Property(
            type = PropertyType.TEXT,
            category = "Macros",
            name = "Bed Buy Repeats",
            description = "How many times to click bed buys (Default 20)"
    )
    public static String bedBuyRepeats = "20";
    @Property(
            type = PropertyType.TEXT,
            category = "Macros",
            name = "Bed Spam Start Delay",
            description = "How long to start spamming since start of the auction in ms (Default 20000)"
    )
    public static String bedSpamStartDelay = "20000";
    @Property(
            type = PropertyType.CHECKBOX,
            category = "Macros",
            name = "Auto Claim",
            description = "Auto Claims all Cofl Relay Flip Items"
    )
    public static boolean autoClaim = false;
    @Property(
            type = PropertyType.CHECKBOX,
            category = "Macros",
            name = "Auto Sell",
            description = "Auto Sells all Cofl Relay Flips"
    )
    public static boolean autoSell = false;
    @Property(
            type = PropertyType.TEXT,
            category = "Macros",
            name = "Auto Sell Time",
            description = "How many time in hours to list items for (Default 48)"
    )
    public static String autoSellTime = "48";
    @Property(
            type = PropertyType.SELECTOR,
            category = "Macros",
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
    public static int autoSellPrice = 1;
    @Property(
            type = PropertyType.CHECKBOX,
            category = "Macros",
            name = "Auto Claim Sold",
            description = "Auto Claims all Sold Auctions"
    )
    public static boolean autoClaimSold = false;
    @Property(
            type = PropertyType.SELECTOR,
            category = "Macros",
            name = "Auto Relist",
            description = "Auto Relists Expired Auctions",
            options = {
                    "Disabled",
                    "Same Price",
                    "New Value"
            }
    )
    public static int autoRelist = 0;

    @Property(
            type = PropertyType.TEXT,
            category = "Failsafes",
            name = "Stuck Menu Delay",
            description = "How many MS before auto closing a gui (Default 15000)",
            options = {
                    "Disabled",
                    "Same Price",
                    "New Value"
            }
    )
    public static String autoCloseMenuDelay = "15000";
    @Property(
            type = PropertyType.CHECKBOX,
            category = "Failsafes",
            name = "Anti Limbo",
            description = "Brings you back to your skyblock island while in limbo (if macro enabled)"
    )
    public static boolean antiLimbo = true;
    @Property(
            type = PropertyType.CHECKBOX,
            category = "Failsafes",
            name = "Auto Reconnect",
            description = "Reconnects to Hypixel when kicked (if macro enabled)"
    )
    public static boolean autoReconnect = true;
    @Property(
            type = PropertyType.CHECKBOX,
            category = "Failsafes",
            name = "Auto Island",
            description = "Reconnects to Island when closed (if macro enabled)"
    )
    public static boolean autoIsland = true;

    @Property(
            type = PropertyType.CHECKBOX,
            category = "Webhooks",
            name = "Flip Bought",
            description = "Webhook whenever a flip is purchased by the macro"
    )
    public static boolean boughtWebhooks = false;
    @Property(
            type = PropertyType.CHECKBOX,
            category = "Webhooks",
            name = "Flip Listed",
            description = "Webhook whenever a flip is listed by the macro"
    )
    public static boolean listedWebhooks = false;
    @Property(
            type = PropertyType.CHECKBOX,
            category = "Webhooks",
            name = "Flip Sold",
            description = "Webhook whenever a flip is sold"
    )
    public static boolean soldWebhooks = false;
    @Property(
            type = PropertyType.CHECKBOX,
            category = "Webhooks",
            name = "24H Stats",
            description = "Sends flip stats every 24 Hours"
    )
    public static boolean statsWebhooks = false;

    @Property(
            type = PropertyType.CHECKBOX,
            category = "Developer Menu",
            name = "Debug Mode",
            description = "Prints Debugging Messages"
    )
    public static boolean debug = false;
    @Property(
            type = PropertyType.TEXT,
            category = "Developer Menu",
            name = "Activation Key",
            description = "Do not change this value unless told to."
    )
    public static String activationKey = "";

    public FlipConfig() {
        super(new File("config/coflflip.toml"), "Cofl Flip Config", new JVMAnnotationPropertyCollector(), new SortingBehavior() {
            @NotNull
            @Override
            public Comparator<? super Category> getCategoryComparator() {
                return (Comparator<Category>) (o1, o2) -> 0;
            }

            @Override
            public @NotNull Comparator<? super Map.Entry<String, ? extends List<PropertyData>>> getSubcategoryComparator() {
                return (Comparator<Map.Entry<String, ? extends List<PropertyData>>>) (o1, o2) -> 0;
            }
        });

        this.preload();
        this.writeData();
        this.initialize();
    }
}
