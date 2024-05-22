package dev.jacktym.coflflip.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class QueueUtil {
    public static List<Runnable> queue = new ArrayList<>();
    private static boolean doingAction = false;
    private static final Lock lock = new ReentrantLock();
    private static final Condition actionCondition = lock.newCondition();

    public static void startQueue() {
        new Thread(() -> {
            try {
                while (true) {
                    if (!queue.isEmpty()) {
                        queue.remove(0).run();
                        waitForActionFinish();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                startQueue();
            }
        }).start();
    }

    public static void finishAction() {
        lock.lock();
        try {
            actionCondition.signal();
        } finally {
            lock.unlock();
        }
    }

    private static void waitForActionFinish() throws InterruptedException {
        lock.lock();
        try {
            while (doingAction) {
                actionCondition.await();
            }
        } finally {
            lock.unlock();
        }
    }

    public static void addToQueue(Runnable action) {
        queue.add(action);
    }
}
