package dev.jacktym.marketshark.util;

import java.util.Timer;
import java.util.TimerTask;

public class DelayUtils {
    private static final Timer timer = new Timer();
    public static TimerTask delayAction(long delay, Runnable action) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                action.run();
            }
        };
        timer.schedule(task, delay);

        return task;
    }
}
