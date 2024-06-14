package dev.jacktym.marketshark.util;

import dev.jacktym.marketshark.config.FlipConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class QueueUtil {
    public static List<Runnable> queue = new ArrayList<>();
    public static String currentAction = "";
    public static ScheduledFuture<?> finishFailsafe;

    public static void finishAction(String action) {
        if (currentAction.equals(action)) {
            finishAction();
        }
    }

    public static void finishAction() {
        System.out.println("Finished " + currentAction);
        currentAction = "";
        if (finishFailsafe != null) {
            finishFailsafe.cancel(true);
            finishFailsafe = null;
        }
        if (!queue.isEmpty()) {
            Runnable r = queue.remove(0);
            currentAction = r.getClass().getSimpleName().split("\\$\\$")[0];
            r.run();
            System.out.println("1 Started " + currentAction);
        }
    }

    public static void addToQueue(Runnable action) {
        System.out.println("Added " + action.getClass().getSimpleName().split("\\$\\$")[0]);
        queue.add(0, action);

        if (currentAction.isEmpty()) {
            Runnable r = queue.remove(0);
            currentAction = r.getClass().getSimpleName().split("\\$\\$")[0];
            r.run();
            System.out.println("2 Started " + currentAction);

            finishFailsafe = DelayUtils.delayAction(Long.parseLong(FlipConfig.autoCloseMenuDelay), () -> finishAction(currentAction));
        }
    }
}
