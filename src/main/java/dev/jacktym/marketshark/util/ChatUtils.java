package dev.jacktym.marketshark.util;

import dev.jacktym.marketshark.Main;
import gg.essential.universal.ChatColor;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class ChatUtils {
    public static void printMarkedChat(String message) {
        addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "[" + EnumChatFormatting.WHITE + "MarketShark" + EnumChatFormatting.GOLD + "] " + EnumChatFormatting.WHITE + message));
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

    public static String abbreviateNumber(long number) {
        if (number < 1000) {
            return Long.toString(number);
        }

        char[] suffixes = new char[] {'k', 'M', 'B', 'T'};
        int magnitude = (int) (Math.log10(number) / 3);
        double truncated = number / Math.pow(1000, magnitude);
        String formatted = String.format("%.1f", truncated);

        if (formatted.endsWith(".0")) {
            formatted = formatted.substring(0, formatted.length() - 2);
        }

        return formatted + suffixes[magnitude - 1];
    }

    public static long unabbreviateNumber(String abbreviated) {
        if (abbreviated == null || abbreviated.isEmpty()) {
            throw new IllegalArgumentException("Invalid input");
        }

        char lastChar = abbreviated.charAt(abbreviated.length() - 1);
        double multiplier = 1;

        switch (lastChar) {
            case 'k':
                multiplier = 1_000;
                break;
            case 'M':
                multiplier = 1_000_000;
                break;
            case 'B':
                multiplier = 1_000_000_000;
                break;
            case 'T':
                multiplier = 1_000_000_000_000L;
                break;
            default:
                if (!Character.isDigit(lastChar)) {
                    throw new IllegalArgumentException("Invalid suffix in the input");
                }
        }

        double number;
        try {
            if (multiplier == 1) {
                number = Double.parseDouble(abbreviated);
            } else {
                number = Double.parseDouble(abbreviated.substring(0, abbreviated.length() - 1)) * multiplier;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format in the input", e);
        }

        return (long) number;
    }
}
