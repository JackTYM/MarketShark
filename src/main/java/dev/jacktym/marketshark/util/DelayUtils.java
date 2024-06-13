package dev.jacktym.marketshark.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DelayUtils {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    public static void delayAction(long delay, Runnable action) {
        scheduler.schedule(() -> {
            try {
                action.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }
}
