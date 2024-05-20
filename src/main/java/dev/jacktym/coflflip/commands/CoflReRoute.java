package dev.jacktym.coflflip.commands;

import com.google.gson.JsonObject;
import dev.jacktym.coflflip.util.ChatUtils;
import dev.jacktym.coflflip.util.CoflWebsocketClient;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import java.util.Arrays;

public class CoflReRoute extends CommandBase {
    @Override
    public String getCommandName() {
        return "cofl";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/cofl";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 0) {
            String command = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

            JsonObject json = new JsonObject();
            json.addProperty("type", args[0]);
            json.addProperty("data", command);
            System.out.println(json.toString());
            CoflWebsocketClient.websocketClient.send(json.toString());
        } else {
            ChatUtils.printMarkedChat("Cofl Routing Enabled");
        }
    }
}
