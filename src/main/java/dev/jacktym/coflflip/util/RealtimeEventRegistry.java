package dev.jacktym.coflflip.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.function.Function;

public class RealtimeEventRegistry {
    public static Multimap<String, Function<Event, Boolean>> eventMap = ArrayListMultimap.create();

    //public static Multimap<Long, Consumer<Event>> timeRegistry = ArrayListMultimap.create();

    @SubscribeEvent
    public void clientTickEvent(TickEvent.ClientTickEvent event) {
        handleEvent("clientTickEvent", event);
    }

    @SubscribeEvent
    public void entityJoinWorldEvent(EntityJoinWorldEvent event) {
        handleEvent("entityJoinWorldEvent", event);
    }

    @SubscribeEvent
    public void guiOpenEvent(GuiOpenEvent event) {
        handleEvent("guiOpenEvent", event);
    }

    private void handleEvent(String eventString, Event event) {
        eventMap.get(eventString).forEach(function -> {
            if (function.apply(event)) {
                removeEvent(eventString, function);
            }
        });
    }

    public static void registerEvent(String event, Function<Event, Boolean> consumer) {
        eventMap.put(event, consumer);
    }

    public static void removeEvent(String eventString, Function<Event, Boolean> event) {
        eventMap.remove(eventString, event);
    }
}
