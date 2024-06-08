package dev.jacktym.coflflip.mixins;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFrame;
import dev.jacktym.coflflip.util.ChatUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(com.neovisionaries.ws.client.ListenerManager.class)
public class WSCommandHandlerMixin {
    @Inject(at = @At(value = "HEAD"), method = "onFrame", remap = false)
    private void onFrame(WebSocket websocket, WebSocketFrame frame, CallbackInfo ci) {
        System.out.println(frame.getPayloadText());
        ChatUtils.printMarkedChat(frame.getPayloadText());
        /*if (cmd.getType() == CommandType.Flip) {
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
        }*/
    }

    @Inject(at = @At(value = "HEAD"), method = "onFrame", remap = false)
    private void HandleCommand(WebSocket websocket, WebSocketFrame frame, CallbackInfo ci) {
        System.out.println(frame.getPayloadText());
        ChatUtils.printMarkedChat(frame.getPayloadText());
    }
}
