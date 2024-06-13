package dev.jacktym.marketshark.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DelayUtils {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    public static ScheduledFuture<?> delayAction(long delay, Runnable action) {
        return scheduler.schedule(() -> {
            try {
                action.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }
}
