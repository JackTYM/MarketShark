package dev.jacktym.coflflip.config;

import dev.jacktym.coflflip.Main;
import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.JVMAnnotationPropertyCollector;
import gg.essential.vigilance.data.Property;
import gg.essential.vigilance.data.PropertyType;

import java.io.File;

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
            category = "Macros",
            name = "Bed Spam Delay",
            description = "How quickly to spam buy bed auctions (Default 100)"
    )
    public static String bedSpamDelay = "100";


    @Property(
            type = PropertyType.TEXT,
            category = "Failsafes",
            name = "Auto Close Menu Delay",
            description = "How many MS before auto closing a buy menu (Default 5000)",
            options = {
                    "Disabled",
                    "Same Price",
                    "New Value"
            }
    )
    public static String autoCloseMenuDelay = "5000";

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
            name = "Session ID",
            description = "DO NOT TOUCH UNLESS YOU KNOW WHAT YOU ARE DOING PLEASE"
    )
    public static String SId = "";
    @Property(
            type = PropertyType.TEXT,
            category = "Developer Menu",
            name = "Session Expiry",
            description = "DO NOT TOUCH UNLESS YOU KNOW WHAT YOU ARE DOING PLEASE"
    )
    public static String sessionExpiresIn = "";
    @Property(
            type = PropertyType.BUTTON,
            category = "Developer Menu",
            name = "Reset Session",
            description = "Resets your CoflNet session. You will need to login again after pressing"
    )
    public final void resetSession() {
        SId = "";
        sessionExpiresIn = "";
        Main.coflWebsocketClient.reconnect();
    }
    @Property(
            type = PropertyType.BUTTON,
            category = "Developer Menu",
            name = "Reconnect COFL",
            description = "Reconnects your CoflNet session"
    )
    public final void reconnectCofl() {
        Main.coflWebsocketClient.reconnect();
    }

    public FlipConfig() {
        super(new File("config/coflflip.toml"), "Cofl Flip Config", new JVMAnnotationPropertyCollector());

        this.preload();
        this.writeData();
        this.initialize();
    }
}
