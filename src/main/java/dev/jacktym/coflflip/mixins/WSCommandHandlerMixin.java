package dev.jacktym.coflflip.mixins;

import de.torui.coflsky.WSCommandHandler;
import de.torui.coflsky.commands.Command;
import de.torui.coflsky.commands.models.FlipData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WSCommandHandler.class)
public class WSCommandHandlerMixin {
    @Inject(at = @At(value = "HEAD"), method = "Flip")
    private static void Flip(Command<FlipData> cmd, CallbackInfo ci) {
        System.out.println("Flip Worth: " + cmd.getData().Worth);
    }
}
