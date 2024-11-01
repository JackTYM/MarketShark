package dev.jacktym.marketshark;

import dev.jacktym.marketshark.commands.MarketShark;
import dev.jacktym.marketshark.config.FlipConfig;
import dev.jacktym.marketshark.macros.AutoClaimSold;
import dev.jacktym.marketshark.macros.Failsafes;
import dev.jacktym.marketshark.util.BugLogger;
import dev.jacktym.marketshark.util.DiscordIntegration;
import dev.jacktym.marketshark.util.FlipItem;
import dev.jacktym.marketshark.util.KeybindHandler;
import dev.jacktym.marketshark.util.RealtimeEventRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Mod(modid = "skyhud", name = "SharketMark", version = "$modVersion")
public class Main {
    public static FlipConfig flipConfig;
    public static Minecraft mc = Minecraft.getMinecraft();
    public static final String version = "$version";
    public static final String modVersion = "$modVersion";
    public static boolean paused = false;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        BugLogger.log("MarketShark Init", true);

        ClientCommandHandler.instance.registerCommand(new MarketShark());

        flipConfig = new FlipConfig();

        List<Object> registry = new ArrayList<>();
        registry.add(new RealtimeEventRegistry());
        registry.add(new AutoClaimSold());
        registry.add(new Failsafes());
        registry.add(new KeybindHandler());
        registry.add(this);

        registry.forEach(MinecraftForge.EVENT_BUS::register);

        Locale.setDefault(new Locale("en", "US"));

        FlipItem.loadFlipData();

        DiscordIntegration.connectToWebsocket();

        KeybindHandler.initializeKeybinds();
    }

    String lastFlips = "";
    @SubscribeEvent
    public void clientTickEvent(TickEvent.ClientTickEvent event) {
        String flipItems = FlipItem.getItemStrings();
        if (!lastFlips.equals(flipItems)) {
            FlipItem.saveFlipData();
            lastFlips = flipItems;
        }
    }
}
