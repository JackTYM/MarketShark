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
