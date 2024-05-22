package dev.jacktym.coflflip.util;

public class DelayUtils {
    public static void delayAction(long delay, Runnable action) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                action.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
