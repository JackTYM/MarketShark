package dev.jacktym.marketshark.commands;

import dev.jacktym.marketshark.Main;
import dev.jacktym.marketshark.macros.AutoList;
import dev.jacktym.marketshark.util.BugLogger;
import dev.jacktym.marketshark.util.ChatUtils;
import dev.jacktym.marketshark.util.DiscordIntegration;
import dev.jacktym.marketshark.util.FlipItem;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MarketShark extends CommandBase {
    @Override
    public String getCommandName() {
        return "marketshark";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("ms", "marketshark", "shark", "sharkmacro", "sm");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/marketshark help";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        List<String> completions = new ArrayList<>();

        if (args.length == 0) {
            completions.add("marketshark");
        } else if (args.length == 1) {
            String partialCommand = args[args.length - 1].toLowerCase(); // Get the last argument and convert to lower case for case insensitive matching
            // Provide top-level command completions
            if ("help".startsWith(partialCommand)) {
                completions.add("help");
            }
            if ("list".startsWith(partialCommand)) {
                completions.add("list");
            }
            if ("pause".startsWith(partialCommand)) {
                completions.add("pause");
            }
            if ("unpause".startsWith(partialCommand)) {
                completions.add("unpause");
            }
            //#if >=GreatWhite
            if ("listinv".startsWith(partialCommand)) {
                completions.add("listinv");
            }
            //#endif >=GreatWhite
            if ("reset".startsWith(partialCommand)) {
                completions.add("reset");
            }
            if ("discord".startsWith(partialCommand)) {
                completions.add("discord");
            }
            if ("flipinfo".startsWith(partialCommand)) {
                completions.add("flipinfo");
            }
            if ("load".startsWith(partialCommand)) {
                completions.add("help");
            }
        }

        return completions;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!DiscordIntegration.activated && args.length != 0) {
            BugLogger.logChat("MarketShark is not activated! Please enter your Activation Key in /ms and click Connect!", true);
            return;
        }
        if (Main.paused) {
            BugLogger.logChat("MarketShark is Paused! Features will not work until unpaused!", true);
        }
        switch (args.length) {
            case 0: {
                Main.flipConfig.openGui();
                break;
            }
            case 1: {
                switch (args[0]) {
                    case "help": {
                        ChatUtils.printColoredChat("MarketShark Help Menu", EnumChatFormatting.GOLD);
                        ChatUtils.printUnmarkedChat("/marketshark - Displays Config GUI");
                        ChatUtils.printUnmarkedChat("/marketshark list - Lists held item");
                        ChatUtils.printUnmarkedChat("/marketshark list (price) - Lists held item at specific price");
                        ChatUtils.printUnmarkedChat("/marketshark pause - Pauses MarketShark features");
                        ChatUtils.printUnmarkedChat("/marketshark unpause - Unpauses MarketShark features");
                        //#if >=GreatWhite
                        ChatUtils.printUnmarkedChat("/marketshark listinv - Lists entire inventory");
                        //#endif >=GreatWhite
                        ChatUtils.printUnmarkedChat("/marketshark reset - Reset to bug fix");
                        ChatUtils.printUnmarkedChat("/marketshark discord - Reconnect to discord");
                        ChatUtils.printUnmarkedChat("/marketshark flipinfo - Prints cached flip info about held item");
                        ChatUtils.printUnmarkedChat("/marketshark load (id) - Loads a config ID");
                        break;
                    }
                    case "list": {
                        FlipItem flipItem = FlipItem.getFlipItem(Main.mc.thePlayer.inventory.getCurrentItem());
                        AutoList.listItem(flipItem, false);
                        break;
                    }
                    case "pause": {
                        if (!Main.paused) {
                            BugLogger.logChat("Paused MarketShark", true);
                            Main.paused = true;
                            DiscordIntegration.Reset();
                        }
                        break;
                    }
                    case "unpause": {
                        if (Main.paused) {
                            BugLogger.logChat("Unpaused MarketShark", true);
                            Main.paused = false;
                        }
                        break;
                    }
                    //#if >=GreatWhite
                    case "listinv": {
                        AutoList.listInventory();
                        break;
                    }
                    //#endif >=GreatWhite
                    case "reset": {
                        DiscordIntegration.Reset();
                        BugLogger.logChat("Reset MarketShark!", true);
                    }
                    case "discord": {
                        DiscordIntegration.connectToWebsocket();
                        break;
                    }
                    case "flipinfo": {
                        FlipItem flipItem = FlipItem.getFlipItem(Main.mc.thePlayer.inventory.getCurrentItem());

                        BugLogger.logChat("Current Flip Item - Buy Price: " + flipItem.buyPrice + " Cofl Worth: " + flipItem.coflWorth + " Bed: " + flipItem.bed + " Uuid: " + flipItem.uuid, true);
                        break;
                    }
                    case "load": {
                        BugLogger.logChat("Usage: /cofl load (id)", true);
                        break;
                    }
                }
                break;
            }
            case 2: {
                if (args[0].equals("list")) {
                    try {
                        FlipItem flipItem = FlipItem.getFlipItem(Main.mc.thePlayer.inventory.getCurrentItem());
                        flipItem.sellPrice = ChatUtils.unabbreviateNumber(args[1].replace(",", ""));
                        AutoList.listItem(flipItem);
                    } catch (Exception e) {
                        BugLogger.logError(e);
                        BugLogger.logChat("Failed to list item. Report this!", true);
                    }
                }
                if (args[0].equals("load")) {
                    DiscordIntegration.sendToWebsocket("LoadConfig", args[1]);
                }
                break;
            }
        }
    }
}
