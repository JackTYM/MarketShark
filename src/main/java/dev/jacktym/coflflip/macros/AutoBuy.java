package dev.jacktym.coflflip.macros;

import dev.jacktym.coflflip.Main;
import dev.jacktym.coflflip.util.RealtimeEventRegistry;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.function.Consumer;

public class AutoBuy {
    public static Thread buyThread;
    public static void autoBuy() {
        if (buyThread == null) {
            buyThread = new Thread(() -> {
                Long registryId = System.currentTimeMillis();
                Consumer<Event> purchaseWaiter = event -> {
                    if (Main.mc.currentScreen instanceof GuiChest) {

                        RealtimeEventRegistry.removeEvent(registryId);
                    }
                };
                RealtimeEventRegistry.registerEvent("guiOpenEvent", purchaseWaiter, registryId);
            });
            buyThread.start();
        } else {
            System.out.println("Stopped AutoBuy. Thread running!");
        }
    }
}
