package dev.jacktym.coflflip.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.function.Consumer;

public class RealtimeEventRegistry {
    public static Multimap<String, Consumer>    eventMap = ArrayListMultimap.create();

    @SubscribeEvent
    public void clientTickEvent(TickEvent.ClientTickEvent event) {
        eventMap.get("clientTickEvent").forEach(consumer -> consumer.accept(event));
    }

    public static void registerEvent(String event, Consumer consumer) {
        eventMap.put(event, consumer);
    }

    public static void removeEvent(String event, Consumer<Object> consumer) {
        eventMap.remove(event, consumer);
    }
}
