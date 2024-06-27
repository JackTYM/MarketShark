package dev.jacktym.marketshark.util;

import java.util.Timer;
import java.util.TimerTask;

public class DelayUtils {
    private static Timer timer = new Timer();
    public static TimerTask delayAction(long delay, Runnable action) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                action.run();
            }
        };
        try {
            timer.schedule(task, delay);

            return task;
        } catch (Exception e) {
            timer = new Timer();
            return delayAction(delay, action);
        }
    }
}
