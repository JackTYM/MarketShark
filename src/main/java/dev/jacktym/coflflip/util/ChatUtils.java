package dev.jacktym.coflflip.util;

import dev.jacktym.coflflip.Main;
import gg.essential.universal.ChatColor;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class ChatUtils {
    public static void printMarkedChat(String message) {
        addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "[" + EnumChatFormatting.GRAY + "CoflFlip" + EnumChatFormatting.GOLD + "]" + EnumChatFormatting.WHITE + message));
    }

    public static void printUnmarkedChat(String message) {
        addChatMessage(new ChatComponentText(message));
    }

    public static void printColoredChat(String message, EnumChatFormatting color) {
        addChatMessage(new ChatComponentText(color + message));
    }

    public static void addChatMessage(IChatComponent component) {
        if (Main.mc.thePlayer != null) {
            Main.mc.thePlayer.addChatMessage(component);
        }
    }

    public static String stripColor(String toStrip) {
        for (ChatColor c : ChatColor.values()) {
            toStrip = toStrip.replaceAll(c.toString(), "");
        }
        return toStrip;
    }
}
