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
import java.util.function.Function;

public class RealtimeEventRegistry {
    public static Multimap<String, Function<Event, Boolean>> eventMap = ArrayListMultimap.create();

    @SubscribeEvent
    public void clientTickEvent(TickEvent.ClientTickEvent event) {
        handleEvent("clientTickEvent", event, 0);
    }

    @SubscribeEvent
    public void entityJoinWorldEvent(EntityJoinWorldEvent event) {
        handleEvent("entityJoinWorldEvent", event, 0);
    }

    @SubscribeEvent
    public void guiScreenEvent(GuiScreenEvent event) {
        handleEvent("guiScreenEvent", event, 0);
    }

    @SubscribeEvent
    public void clientChatReceivedEvent(ClientChatReceivedEvent event) { handleEvent("clientChatReceivedEvent", event, 0); }

    private void handleEvent(String eventString, Event event, int i) {
        if (i > 10) {
            System.out.println("ConcurrentModification StackOverflow. Clearing events!");
            eventMap.clear();
            return;
        }
        try {
            eventMap.get(eventString).removeIf(function -> function.apply(event));
        } catch (ConcurrentModificationException e) {
            handleEvent(eventString, event, i+1);
        }
    }

    public static void registerEvent(String event, Function<Event, Boolean> consumer) {
        eventMap.put(event, consumer);
    }

    public static void removeEvent(String eventString, Function<Event, Boolean> event) {
        eventMap.remove(eventString, event);
    }
}
