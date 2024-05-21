package dev.jacktym.coflflip.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
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
    public void guiScreenEvent(GuiScreenEvent event) {
        handleEvent("guiScreenEvent", event);
    }

    @SubscribeEvent
    public void clientChatReceivedEvent(ClientChatReceivedEvent event) { handleEvent("clientChatReceivedEvent", event); }

    private void handleEvent(String eventString, Event event) {
        try {
            Iterator<Function<Event, Boolean>> iterator = eventMap.get(eventString).iterator();
            while (iterator.hasNext()) {
                Function<Event, Boolean> function = iterator.next();
                if (function.apply(event)) {
                    iterator.remove();
                }
            }
        } catch (ConcurrentModificationException e) {
            handleEvent(eventString, event);
        }
    }

    public static void registerEvent(String event, Function<Event, Boolean> consumer) {
        eventMap.put(event, consumer);
    }

    public static void removeEvent(String eventString, Function<Event, Boolean> event) {
        eventMap.remove(eventString, event);
    }
}
