package dev.jacktym.coflflip;

import dev.jacktym.coflflip.commands.CoflFlip;
import dev.jacktym.coflflip.config.FlipConfig;
import dev.jacktym.coflflip.macros.AutoClaimSold;
import dev.jacktym.coflflip.util.CoflClient;
import dev.jacktym.coflflip.util.DiscordIntegration;
import dev.jacktym.coflflip.util.RealtimeEventRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import java.io.PrintStream;
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
        registry.forEach(MinecraftForge.EVENT_BUS::register);

        Locale.setDefault(new Locale("en", "US"));

        System.setOut(new PrintStream(System.out) {
            @Override
            public void println(String str) {
                CoflClient.handleMessage(str);
                super.println(str);
            }
        });

        DiscordIntegration.connectToWebsocket();
    }
}
