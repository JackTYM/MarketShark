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
        try {
            if (Main.paused || !DiscordIntegration.activated) {
                return;
            }
            DelayUtils.delayAction(FlipConfig.autoCloseMenuDelay, () -> {
                if (Main.mc != null && Main.mc.thePlayer != null && FlipConfig.autoOpen && event.gui == Main.mc.currentScreen && event.gui instanceof GuiChest) {
                    BugLogger.logChat("Stuck GUI Failsafe Triggered!", true);
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
        } catch (Exception e) {
            BugLogger.logError(e);
        }
    }

    public static boolean stuckEventFailsafe(TickEvent.ClientTickEvent event, long startTime, String clazz) {
        if (startTime + FlipConfig.autoCloseMenuDelay < System.currentTimeMillis() && Main.mc != null && Main.mc.thePlayer != null && FlipConfig.autoOpen) {
            BugLogger.logChat("Stuck Event Failsafe Triggered!", true);
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
        if (Main.paused || !DiscordIntegration.activated) {
            return;
        }
        if (FlipConfig.autoOpen) {
            String message = ChatUtils.stripColor(event.message.getUnformattedText());

            if ((
                    message.equals("You were spawned in Limbo.")
                            || message.equals("You are AFK. Move around to return from AFK.")
                            || message.startsWith("A kick occurred in your")
            ) && FlipConfig.antiLimbo) {
                BugLogger.logChat("In Limbo. Rejoining Skyblock in 20 seconds!", true);
                Main.mc.thePlayer.sendChatMessage("/l");

                DelayUtils.delayAction(20000, () -> {
                    Main.mc.thePlayer.sendChatMessage("/skyblock");

                    DelayUtils.delayAction(5000, () -> {
                        Main.mc.thePlayer.sendChatMessage("/is");
                    });
                });
            } else if ((message.startsWith("Evacuating to Hub..") || message.startsWith("You are being transferred to the HUB for being AFK!")) && FlipConfig.autoIsland) {
                BugLogger.logChat("Island closed. Rejoining in 5 Seconds!", true);
                DelayUtils.delayAction(5000, () -> {
                    Main.mc.thePlayer.sendChatMessage("/is");
                });
            }
        }
    }

    static ServerData lastServerData;

    @SubscribeEvent
    public void connectToServerEvent(EntityJoinWorldEvent event) {
        if (Main.paused || !DiscordIntegration.activated) {
            return;
        }
        if (event.entity == Main.mc.thePlayer) {
            if (Main.mc.getCurrentServerData() != null) {
                lastServerData = Main.mc.getCurrentServerData();
            }
        }
    }

    @SubscribeEvent
    public void serverDisconnectGui(GuiOpenEvent event) {
        try {
            if (Main.paused || !DiscordIntegration.activated) {
                return;
            }
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
        } catch (Exception e) {
            BugLogger.logError(e);
        }
    }

    // Required to use a thread with OpenGL context
    private static boolean connect = false;
    @SubscribeEvent
    public void GuiEvent(GuiScreenEvent event) {
        if (Main.paused || !DiscordIntegration.activated) {
            return;
        }
        if (connect) {
            connect = false;
            FMLClientHandler.instance().connectToServer(new GuiMainMenu(), lastServerData);

            DelayUtils.delayAction(5000, () -> {
                if (Main.mc.getCurrentServerData() != null) {
                    try {
                        BugLogger.logChat("Reconnected! Going to Skyblock Island!", true);
                        DelayUtils.delayAction(5000, () -> {
                            Main.mc.thePlayer.sendChatMessage("/skyblock");

                            DelayUtils.delayAction(5000, () -> {
                                Main.mc.thePlayer.sendChatMessage("/is");
                            });
                        });
                    } catch (Exception e) {
                        BugLogger.logError(e);
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
