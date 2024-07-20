package dev.jacktym.marketshark.mixins;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketState;
import de.torui.coflsky.commands.Command;
import de.torui.coflsky.commands.CommandType;
import de.torui.coflsky.commands.JsonStringCommand;
import de.torui.coflsky.commands.models.ChatMessageData;
import dev.jacktym.marketshark.macros.AutoOpen;
import dev.jacktym.marketshark.util.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Stream;

@Mixin(targets = "com.neovisionaries.ws.client.ListenerManager")
public class ListenerManagerMixin {
    @Shadow
    private WebSocket mWebSocket;

    @Inject(at = @At(value = "HEAD"), method = "callOnTextMessage", remap = false)
    private void callOnTextMessage(String message, CallbackInfo ci) {
        try {
            JsonStringCommand cmd = new Gson().fromJson(message, JsonStringCommand.class);

            BugLogger.log("Cofl Message: " + cmd.getType().name(), true);
            if (cmd.getType() == CommandType.Flip) {
                try {
                    FlipData flip = cmd.GetAs(new TypeToken<FlipData>() {
                    }).getData();

                    FlipItem item = new FlipItem();
                    item.auctionId = flip.auctionId;
                    item.coflWorth = flip.coflWorth;
                    item.uuid = flip.auctionData.uuid;
                    item.sellerUuid = flip.auctionData.sellerUuid;
                    item.skyblockId = flip.auctionData.skyblockId;
                    item.finder = flip.finder;
                    item.bed = false;
                    // Supports Multiple Cofl Date Formats
                    try {
                        item.auctionStart = OffsetDateTime.parse(flip.auctionData.start, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant().toEpochMilli();
                    } catch (Exception e) {
                        //BugLogger.logError(e);
                    }
                    try {
                        item.auctionStart = DateTimeFormatter.ISO_INSTANT.parse(flip.auctionData.start, Instant::from).toEpochMilli();
                    } catch (Exception e) {
                        //BugLogger.logError(e);
                    }
                    AutoOpen.openAuction(item);
                } catch (Exception e) {
                    BugLogger.logError(e);
                }
            } else if (cmd.getType() == CommandType.ChatMessage) {
                try {
                    Command<ChatMessageData[]> data = cmd.GetAs(new TypeToken<ChatMessageData[]>() {
                    });

                    ChatMessageData[] list = data.getData();

                    Stream<String> stream = Arrays.stream(list).map((msg) -> msg.Text);
                    String fullMessage = String.join(",", stream.toArray(String[]::new));

                    RealtimeEventRegistry.handleMessage("coflMessage", fullMessage, 0);
                } catch (Exception e) {
                    BugLogger.logError(e);
                }
            }
            // Incase the message is a Cofl Captcha
            DiscordIntegration.getCoflCaptcha(message);
        } catch (Exception ignored) {
            // Ignore. Not a command from Cofl
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "callOnStateChanged", remap = false)
    private void callOnStateChanged(WebSocketState newState, CallbackInfo ci) {
        BugLogger.log("WebSocket State Changed: " + newState.name() + " | " + mWebSocket.getURI().toString(), true);
    }
}
