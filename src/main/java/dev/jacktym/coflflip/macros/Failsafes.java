package dev.jacktym.coflflip.macros;

import dev.jacktym.coflflip.Main;
import dev.jacktym.coflflip.config.FlipConfig;
import dev.jacktym.coflflip.util.QueueUtil;
import dev.jacktym.coflflip.util.RealtimeEventRegistry;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Failsafes {
    public static boolean closeGuiFailsafe(TickEvent.ClientTickEvent event, long expiryTime, String clazz) {
        if (expiryTime < System.currentTimeMillis()) {
            if (FlipConfig.debug) {
                System.out.println("Stuck GUI Failsafe Triggered!");
            }
            RealtimeEventRegistry.clearClazzMap(clazz);
            Main.mc.thePlayer.closeScreen();
            QueueUtil.finishAction();
            return true;
        }
        return false;
    }
}
