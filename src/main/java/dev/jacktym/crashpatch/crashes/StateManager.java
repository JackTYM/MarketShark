package dev.jacktym.crashpatch.crashes;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class StateManager {
    // Use WeakReference to allow garbage collection, preventing memory leaks
    public static final Set<WeakReference<IResettable>> resettableRefs = new HashSet<>();

    public static void resetStates() {
        Iterator<WeakReference<IResettable>> iterator = resettableRefs.iterator();
        while (iterator.hasNext()) {
            WeakReference<IResettable> ref = iterator.next();
            IResettable resettable = ref.get();
            if (resettable != null) {
                resettable.resetState();
            } else {
                iterator.remove();
            }
        }
    }

    public static void registerResettable(IResettable resettable) {
        resettableRefs.add(new WeakReference<>(resettable));
    }

    public interface IResettable {
        void resetState();
    }
}