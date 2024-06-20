package dev.jacktym.marketshark.util;

import java.util.Timer;
import java.util.TimerTask;

public class DelayUtils {
    public static Timer delayAction(long delay, Runnable action) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                action.run();
            }
        }, delay);

        return timer;
    }
}
