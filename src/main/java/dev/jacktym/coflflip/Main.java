package dev.jacktym.coflflip;

import dev.jacktym.coflflip.commands.CoflFlip;
import dev.jacktym.coflflip.config.FlipConfig;
import dev.jacktym.coflflip.macros.AutoClaimSold;
import dev.jacktym.coflflip.macros.Failsafes;
import dev.jacktym.coflflip.util.DiscordIntegration;
import dev.jacktym.coflflip.util.FlipItem;
import dev.jacktym.coflflip.util.RealtimeEventRegistry;
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

@Mod(modid = "coflflip", name = "CoflFlip", version = "1.0.0")
public class Main {
    public static Main instance = new Main();
    public static FlipConfig flipConfig;
    public static Minecraft mc = Minecraft.getMinecraft();

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new CoflFlip());

        flipConfig = new FlipConfig();

        List<Object> registry = new ArrayList<>();
        registry.add(new RealtimeEventRegistry());
        registry.add(new AutoClaimSold());
        registry.add(new Failsafes());
        registry.add(this);
        registry.forEach(MinecraftForge.EVENT_BUS::register);

        RealtimeEventRegistry.registerPacket(Failsafes::receivePacket, "Failsafes");

        Locale.setDefault(new Locale("en", "US"));

        /*System.setOut(new PrintStream(System.out) {
            @Override
            public void println(String str) {
                CoflClient.handleMessage(str);
                super.println(str);
            }
        });*/
        FlipItem.loadFlipData();

        DiscordIntegration.connectToWebsocket();
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
