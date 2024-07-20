package dev.jacktym.marketshark.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.jacktym.marketshark.Main;
import dev.jacktym.marketshark.config.FlipConfig;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.network.Packet;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.function.Function;

public class RealtimeEventRegistry {
    public static Multimap<String, Function<Event, Boolean>> eventMap = ArrayListMultimap.create();
    public static Multimap<String, Map.Entry<String, Function<Event, Boolean>>> classMap = ArrayListMultimap.create();
    public static ArrayList<Function<Packet, Boolean>> packetArray = new ArrayList<>();
    public static Multimap<String, Function<Packet, Boolean>> packetClassMap = ArrayListMultimap.create();
    public static Multimap<String, Function<String, Boolean>> messageMap = ArrayListMultimap.create();
    public static Multimap<String, Map.Entry<String, Function<String, Boolean>>> messageClassMap = ArrayListMultimap.create();

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
    public void clientChatReceivedEvent(ClientChatReceivedEvent event) {
        handleEvent("clientChatReceivedEvent", event, 0);
    }

    public static void handleEvent(String eventString, Event event, int i) {
        if (i > 10) {
            BugLogger.log("ConcurrentModification StackOverflow. Clearing events!", FlipConfig.debug);
            eventMap.clear();
            return;
        }
        try {
            eventMap.get(eventString).removeIf(function -> function.apply(event));
        } catch (ConcurrentModificationException e) {
            handleEvent(eventString, event, i + 1);
        }
    }

    public static void handlePacket(Packet packet, int i) {
        if (i > 10) {
            BugLogger.log("ConcurrentModification StackOverflow. Clearing events!", FlipConfig.debug);
            packetArray.clear();
            return;
        }
        try {
            packetArray.removeIf(function -> function.apply(packet));
        } catch (ConcurrentModificationException e) {
            handlePacket(packet, i + 1);
        }
    }

    public static void handleMessage(String messageString, String message, int i) {
        if (i > 10) {
            BugLogger.log("ConcurrentModification StackOverflow. Clearing events!", FlipConfig.debug);
            eventMap.clear();
            return;
        }
        try {
            messageMap.get(messageString).removeIf(function -> function.apply(message));
        } catch (ConcurrentModificationException e) {
            handleMessage(messageString, message, i + 1);
        }
    }

    public static void registerEvent(String event, Function<Event, Boolean> consumer, String clazz) {
        if (!eventMap.containsValue(consumer)) {
            eventMap.put(event, consumer);
            classMap.put(clazz, new AbstractMap.SimpleEntry<>(event, consumer));
        }
    }

    public static void registerPacket(Function<Packet, Boolean> consumer, String clazz) {
        packetArray.add(consumer);
        packetClassMap.put(clazz, consumer);
    }

    public static void registerMessage(String message, Function<String, Boolean> consumer, String clazz) {
        messageMap.put(message, consumer);
        messageClassMap.put(clazz, new AbstractMap.SimpleEntry<>(message, consumer));
    }

    public static void clearClazzMap(String clazz) {
        if (classMap.containsKey(clazz)) {
            classMap.get(clazz).forEach(entry -> eventMap.remove(entry.getKey(), entry.getValue()));
            classMap.removeAll(clazz);
        }
        if (packetClassMap.containsKey(clazz)) {
            packetClassMap.get(clazz).forEach(entry -> packetArray.remove(entry));
            packetClassMap.removeAll(clazz);
        }
        if (messageClassMap.containsKey(clazz)) {
            messageClassMap.get(clazz).forEach(entry -> messageMap.remove(entry.getKey(), entry.getValue()));
            messageClassMap.removeAll(clazz);
        }
        QueueUtil.finishAction(clazz);
        if (Main.mc.currentScreen instanceof GuiChest) {
            Main.mc.thePlayer.closeScreen();
        }
    }
}
