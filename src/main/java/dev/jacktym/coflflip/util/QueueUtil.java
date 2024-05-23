package dev.jacktym.coflflip.util;

import java.util.ArrayList;
import java.util.List;

public class QueueUtil {
    public static List<Runnable> queue = new ArrayList<>();
    public static boolean doingAction = false;

    public static void finishAction() {
        doingAction = false;
        if (!queue.isEmpty()) {
            queue.remove(0).run();
            doingAction = true;
        }
    }

    public static void addToQueue(Runnable action) {
        queue.add(action);

        if (!doingAction) {
            queue.remove(0).run();
        }
    }
}
