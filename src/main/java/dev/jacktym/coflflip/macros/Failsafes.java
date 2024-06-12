package dev.jacktym.coflflip.macros;

import com.google.gson.JsonObject;
import dev.jacktym.coflflip.Main;
import dev.jacktym.coflflip.config.FlipConfig;
import dev.jacktym.coflflip.util.*;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.Packet;
import net.minecraft.network.login.server.S00PacketDisconnect;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Failsafes {
    @SubscribeEvent
    public void closeGuiFailsafe(GuiScreenEvent event) {
        DelayUtils.delayAction(Long.parseLong(FlipConfig.autoCloseMenuDelay), () -> {
            if (Main.mc != null && Main.mc.thePlayer != null && FlipConfig.autoOpen && event.gui == Main.mc.currentScreen && event.gui instanceof GuiChest) {
                ChatUtils.printMarkedChat("Stuck GUI Failsafe Triggered!");
                if (!QueueUtil.currentAction.isEmpty()) {
                    RealtimeEventRegistry.clearClazzMap(QueueUtil.currentAction);
                }
                if (Main.mc.thePlayer != null) {
                    Main.mc.thePlayer.closeScreen();
                }

                JsonObject response = new JsonObject();
                response.addProperty("message", "Stuck GUI Failsafe Triggered!");
                response.addProperty("username", Main.mc.getSession().getUsername());

                DiscordIntegration.sendToWebsocket("FailsafeTriggered", response.toString());
            }
        });
    }

    public static boolean stuckEventFailsafe(TickEvent.ClientTickEvent event, long startTime, String clazz) {
        if (startTime + Long.parseLong(FlipConfig.autoCloseMenuDelay) < System.currentTimeMillis() && Main.mc != null && Main.mc.thePlayer != null && FlipConfig.autoOpen) {
            ChatUtils.printMarkedChat("Stuck Event Failsafe Triggered!");
            RealtimeEventRegistry.clearClazzMap(clazz);
            Main.mc.thePlayer.closeScreen();

            JsonObject response = new JsonObject();
            response.addProperty("message", "Stuck Event Failsafe Triggered!");
            response.addProperty("username", Main.mc.getSession().getUsername());

            DiscordIntegration.sendToWebsocket("FailsafeTriggered", response.toString());
        }
        return false;
    }

    @SubscribeEvent
    public void clientChatReceivedEvent(ClientChatReceivedEvent event) {
        if (FlipConfig.autoOpen) {
            String message = ChatUtils.stripColor(event.message.getUnformattedText());

            if (message.equals("You are AFK. Move around to return from AFK.") && FlipConfig.antiLimbo) {
                ChatUtils.printMarkedChat("In Limbo. Rejoining Skyblock!");
                Main.mc.thePlayer.sendChatMessage("/l");

                DelayUtils.delayAction(5000, () -> {
                    Main.mc.thePlayer.sendChatMessage("/skyblock");

                    DelayUtils.delayAction(5000, () -> {
                        Main.mc.thePlayer.sendChatMessage("/is");
                    });
                });
            } else if (message.startsWith("Evacuating to Hub..") && FlipConfig.autoIsland) {
                ChatUtils.printMarkedChat("Island closed. Rejoining in 5 Seconds!");
                DelayUtils.delayAction(5000, () -> {
                    Main.mc.thePlayer.sendChatMessage("/is");
                });
            }
        }
    }

    public static boolean receivePacket(Packet packet) {
        if (FlipConfig.autoOpen) {
            if (FlipConfig.autoReconnect) {
                if (packet instanceof S40PacketDisconnect) {
                    S40PacketDisconnect disconnect = (S40PacketDisconnect) packet;

                    System.out.println(disconnect.getReason().getUnformattedText());
                    String reason = disconnect.getReason().getUnformattedText();
                    if (!reason.contains("Reason: ")) {
                        ServerData lastServerData = Main.mc.getCurrentServerData();

                        if (lastServerData != null) {
                            FMLClientHandler.instance().connectToServer(new GuiMainMenu(), lastServerData);
                        }
                    }
                } else if (packet instanceof S00PacketDisconnect) {
                    S00PacketDisconnect disconnect = (S00PacketDisconnect) packet;

                    String reason = disconnect.func_149603_c().getUnformattedText();
                    if (!reason.contains("Reason: ")) {
                        ServerData lastServerData = Main.mc.getCurrentServerData();

                        if (lastServerData != null) {
                            FMLClientHandler.instance().connectToServer(new GuiMainMenu(), lastServerData);
                        }
                    }
                }
            }
        }
        return false;
    }
}
