package dev.jacktym.marketshark.commands;

import dev.jacktym.marketshark.Main;
import dev.jacktym.marketshark.macros.AutoList;
import dev.jacktym.marketshark.util.*;
import gg.essential.api.utils.GuiUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

public class MarketShark extends CommandBase {
    @Override
    public String getCommandName() {
        return "marketshark";
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
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
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
                        break;
                    }
                    case "list": {
                        FlipItem flipItem = FlipItem.getFlipItem(Main.mc.thePlayer.inventory.getCurrentItem());
                        AutoList.listItem(flipItem);
                        break;
                    }
                    case "listinv": {
                        AutoList.listInventory();
                        break;
                    }
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
