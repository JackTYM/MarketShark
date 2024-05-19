package dev.jacktym.coflflip.util;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.function.Consumer;

public class WebsocketClient extends WebSocketClient {
    private final Consumer<ServerHandshake> onOpen;
    private final Consumer<String> onMessage;
    private final Consumer<String> onClose;
    private final Consumer<Exception> onError;

    public WebsocketClient(URI serverUri, Consumer<ServerHandshake> onOpen, Consumer<String> onMessage, Consumer<String> onClose, Consumer<Exception> onError) {
        super(serverUri);
        this.onOpen = onOpen;
        this.onMessage = onMessage;
        this.onClose = onClose;
        this.onError = onError;
        this.connect();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        if (onOpen != null) {
            onOpen.accept(handshakedata);
        }
    }

    @Override
    public void onMessage(String message) {
        if (onMessage != null) {
            onMessage.accept(message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (onClose != null) {
            onClose.accept(reason);
        }
    }

    @Override
    public void onError(Exception ex) {
        if (onError != null) {
            onError.accept(ex);
        }
    }
}
