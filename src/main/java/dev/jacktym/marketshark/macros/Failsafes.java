package dev.jacktym.marketshark.macros;

import com.google.gson.JsonObject;
import dev.jacktym.marketshark.Main;
import dev.jacktym.marketshark.config.FlipConfig;
import dev.jacktym.marketshark.mixins.GuiDisconnectedAccessor;
import dev.jacktym.marketshark.util.*;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
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

            DiscordIntegration.sendToWebsocket("FailsafeTriggered", response.toString());
        }
        return false;
    }

    @SubscribeEvent
    public void clientChatReceivedEvent(ClientChatReceivedEvent event) {
        if (FlipConfig.autoOpen) {
            String message = ChatUtils.stripColor(event.message.getUnformattedText());

            if ((
                    message.equals("You were spawned in Limbo.")
                            || message.equals("You are AFK. Move around to return from AFK.")
                            || message.startsWith("A kick occurred in your")
            ) && FlipConfig.antiLimbo) {
                ChatUtils.printMarkedChat("In Limbo. Rejoining Skyblock!");
                Main.mc.thePlayer.sendChatMessage("/l");

                DelayUtils.delayAction(5000, () -> {
                    Main.mc.thePlayer.sendChatMessage("/skyblock");

                    DelayUtils.delayAction(5000, () -> {
                        Main.mc.thePlayer.sendChatMessage("/is");
                    });
                });
            } else if ((message.startsWith("Evacuating to Hub..") || message.startsWith("You are being transferred to the HUB for being AFK!")) && FlipConfig.autoIsland) {
                ChatUtils.printMarkedChat("Island closed. Rejoining in 5 Seconds!");
                DelayUtils.delayAction(5000, () -> {
                    Main.mc.thePlayer.sendChatMessage("/is");
                });
            }
        }
    }

    static ServerData lastServerData;

    @SubscribeEvent
    public void connectToServerEvent(EntityJoinWorldEvent event) {
        if (event.entity == Main.mc.thePlayer) {
            if (Main.mc.getCurrentServerData() != null) {
                lastServerData = Main.mc.getCurrentServerData();
            }
        }
    }

    @SubscribeEvent
    public void serverDisconnectGui(GuiOpenEvent event) {
        if (event.gui instanceof GuiDisconnected) {
            GuiDisconnectedAccessor disconnectScreen = (GuiDisconnectedAccessor) event.gui;

            String disconnectMessage = disconnectScreen.getMessage().getUnformattedText();
            if (disconnectMessage.contains("Reason: ")) {
                DiscordIntegration.sendToWebsocket("Banned", disconnectMessage);
            } else {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("message", disconnectMessage);
                jsonObject.addProperty("reconnecting", FlipConfig.autoReconnect && FlipConfig.autoOpen);

                DiscordIntegration.sendToWebsocket("Disconnected", jsonObject.toString());

                if (FlipConfig.autoReconnect && FlipConfig.autoOpen) {
                    if (lastServerData != null) {
                        System.out.println("Last Server Data: " + lastServerData.serverIP);
                        DelayUtils.delayAction(5000, () -> connect = true);
                    } else {
                        System.out.println("Last Server Null");
                    }
                }
            }

        }
    }

    // Required to use a thread with OpenGL context
    private static boolean connect = false;
    @SubscribeEvent
    public void GuiEvent(GuiScreenEvent event) {
        if (connect) {
            connect = false;
            FMLClientHandler.instance().connectToServer(new GuiMainMenu(), lastServerData);

            DelayUtils.delayAction(5000, () -> {
                if (Main.mc.getCurrentServerData() != null) {
                    try {
                        ChatUtils.printMarkedChat("Reconnected! Going to Skyblock Island!");
                        DelayUtils.delayAction(5000, () -> {
                            Main.mc.thePlayer.sendChatMessage("/skyblock");

                            DelayUtils.delayAction(5000, () -> {
                                Main.mc.thePlayer.sendChatMessage("/is");
                            });
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            DelayUtils.delayAction(15000, () -> {
                if (Main.mc.getCurrentServerData() == null) {
                    DiscordIntegration.sendToWebsocket("FailedReconnect", "");
                    DelayUtils.delayAction(15000, () -> connect = true);
                }
            });
        }
    }
}
