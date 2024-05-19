package dev.jacktym.coflflip.commands;

import dev.jacktym.coflflip.Main;
import dev.jacktym.coflflip.util.ChatUtils;
import gg.essential.api.utils.GuiUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

public class CoflFlip extends CommandBase {
    @Override
    public String getCommandName() {
        return "coflflip";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/coflflip help";
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
                        ChatUtils.printColoredChat("CoflFlip Help Menu", EnumChatFormatting.GOLD);
                        ChatUtils.printUnmarkedChat("/coflflip - Displays Config GUI");
                    }
                }
            }
        }
    }
}
