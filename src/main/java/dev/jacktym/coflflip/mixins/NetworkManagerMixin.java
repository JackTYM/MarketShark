package dev.jacktym.coflflip.mixins;

import dev.jacktym.coflflip.macros.AutoBuy;
import dev.jacktym.coflflip.util.QueueUtil;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class NetworkManagerMixin {
    @Inject(at = @At(value = "HEAD"), method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V")
    private void channelRead0(ChannelHandlerContext channel, Packet packet, CallbackInfo info) {
        if (QueueUtil.currentAction.equals("AutoBuy")) {
            AutoBuy.receivePacket(packet);
        }
    }
}
