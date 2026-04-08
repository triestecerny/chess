package client;

import com.google.gson.Gson;
import websocket.messages.ServerMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.ErrorMessage;
import websocket.messages.NotificationMessage;

import jakarta.websocket.*;
import java.net.URI;

@ClientEndpoint
public class WebSocketCommunicator {

    private Session session;
    private final ServerMessageObserver observer;
    private final Gson gson = new Gson();

    public interface ServerMessageObserver {
        void onMessage(ServerMessage message);
    }
    public WebSocketCommunicator(String serverUrl, ServerMessageObserver observer) throws Exception {
        this.observer = observer;
        URI uri = new URI(serverUrl.replace("http", "ws") + "/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, uri);
        Thread.sleep(300);
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message) {
        ServerMessage base = gson.fromJson(message, ServerMessage.class);
        ServerMessage typed = switch (base.getServerMessageType()) {
            case LOAD_GAME -> gson.fromJson(message, LoadGameMessage.class);
            case ERROR -> gson.fromJson(message, ErrorMessage.class);
            case NOTIFICATION -> gson.fromJson(message, NotificationMessage.class);
        };
        observer.onMessage(typed);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        this.session = null;
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("WS error: " + throwable.getMessage());
    }

    public void sendMessage(String message) throws Exception {
        session.getBasicRemote().sendText(message);
    }

    public void close() throws Exception {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }
}