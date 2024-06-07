package dev.jacktym.coflflip.mixins;

import com.google.gson.reflect.TypeToken;
import de.torui.coflsky.WSCommandHandler;
import de.torui.coflsky.commands.Command;
import de.torui.coflsky.commands.CommandType;
import de.torui.coflsky.commands.JsonStringCommand;
import dev.jacktym.coflflip.macros.AutoOpen;
import dev.jacktym.coflflip.util.FlipData;
import dev.jacktym.coflflip.util.FlipItem;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Mixin(WSCommandHandler.class)
public class WSCommandHandlerMixin {
    @Inject(at = @At(value = "HEAD"), method = "HandleCommand", remap = false)
    private static void HandleCommand(JsonStringCommand cmd, Entity sender, CallbackInfoReturnable<Boolean> ci) {
        if (cmd.getType() == CommandType.Flip) {
            FlipData flip = cmd.GetAs(new TypeToken<FlipData>() {
            }).getData();

            FlipItem item = new FlipItem();
            item.auctionId = flip.auctionId;
            item.coflWorth = flip.coflWorth;
            item.uuid = flip.auctionData.uuid;
            item.sellerUuid = flip.auctionData.sellerUuid;
            item.skyblockId = flip.auctionData.skyblockId;
            item.bed = false;
            item.auctionStart = DateTimeFormatter.ISO_INSTANT.parse(flip.auctionData.start, Instant::from).toEpochMilli();
            AutoOpen.openAuction(item);
        }
    }
}
