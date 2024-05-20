package dev.jacktym.coflflip.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.function.Consumer;

public class RealtimeEventRegistry {
    public static Multimap<String, Consumer<Event>> eventMap = ArrayListMultimap.create();

    public static Multimap<Long, Consumer<Event>> timeRegistry = ArrayListMultimap.create();

    @SubscribeEvent
    public void clientTickEvent(TickEvent.ClientTickEvent event) {
        eventMap.get("clientTickEvent").forEach(consumer -> consumer.accept(event));
    }

    @SubscribeEvent
    public void entityJoinWorldEvent(EntityJoinWorldEvent event) {
        eventMap.get("entityJoinWorldEvent").forEach(consumer -> consumer.accept(event));
    }

    @SubscribeEvent
    public void guiOpenEvent(GuiOpenEvent event) {
        eventMap.get("guiOpenEvent").forEach(consumer -> consumer.accept(event));
    }

    public static void registerEvent(String event, Consumer<Event> consumer, Long registryId) {
        eventMap.put(event, consumer);
        timeRegistry.put(registryId, consumer);
    }

    public static void removeEvent(String event, Long registryId) {
        eventMap.remove(event, timeRegistry.get(registryId));
        timeRegistry.removeAll(registryId);
    }
}
