package dev.jacktym.marketshark.commands;

import dev.jacktym.marketshark.Main;
import dev.jacktym.marketshark.macros.AutoList;
import dev.jacktym.marketshark.util.*;
import gg.essential.api.utils.GuiUtil;
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
        }

        return completions;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        switch (args.length) {
            case 0: {
                GuiUtil.open(Main.flipConfig.gui());
                break;
            }
            case 1: {
                switch (args[0]) {
                    case "help": {
                        ChatUtils.printColoredChat("MarketShark Help Menu", EnumChatFormatting.GOLD);
                        ChatUtils.printUnmarkedChat("/marketshark - Displays Config GUI");
                        ChatUtils.printUnmarkedChat("/marketshark list - Lists held item");
                        //#if >=GreatWhite
                        ChatUtils.printUnmarkedChat("/marketshark listinv - Lists entire inventory");
                        //#endif >=GreatWhite
                        ChatUtils.printUnmarkedChat("/marketshark reset - Reset to bug fix");
                        ChatUtils.printUnmarkedChat("/marketshark discord - Reconnect to discord");
                        ChatUtils.printUnmarkedChat("/marketshark flipinfo - Prints cached flip info about held item");
                        break;
                    }
                    case "list": {
                        FlipItem flipItem = FlipItem.getFlipItem(Main.mc.thePlayer.inventory.getCurrentItem());
                        AutoList.listItem(flipItem);
                        break;
                    }
                    //#if >=GreatWhite
                    case "listinv": {
                        AutoList.listInventory();
                        break;
                    }
                    //#endif >=GreatWhite
                    case "reset": {
                        RealtimeEventRegistry.eventMap.clear();
                        RealtimeEventRegistry.classMap.clear();
                        RealtimeEventRegistry.eventMap.clear();
                        RealtimeEventRegistry.packetClassMap.clear();
                        AutoList.listingInv = false;
                        AutoList.finishCurrentListing();
                        QueueUtil.queue.clear();
                        QueueUtil.finishAction();
                        break;
                    }
                    case "discord": {
                        DiscordIntegration.connectToWebsocket();
                        break;
                    }
                    case "flipinfo": {
                        FlipItem flipItem = FlipItem.getFlipItem(Main.mc.thePlayer.inventory.getCurrentItem());

                        ChatUtils.printMarkedChat("Current Flip Item - Buy Price: " + flipItem.buyPrice + " Cofl Worth: " + flipItem.coflWorth + " Bed: " + flipItem.bed + " Uuid: " + flipItem.uuid);
                        break;
                    }
                }
            }
        }
    }
}
