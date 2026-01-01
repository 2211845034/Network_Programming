package chatserver;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import java.net.InetSocketAddress;

public class ChatServer extends WebSocketServer {

    public ChatServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        // Updated to English
        System.out.println("New connection from: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // Broadcast logic: Send message to everyone except the sender
        for (WebSocket sock : getConnections()) {
            if (sock != conn) {
                sock.send(message);
            }
        }
        // Log message received to console
        System.out.println("Message relayed from " + conn.getRemoteSocketAddress() + ": " + message);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // Updated to English
        System.out.println("Connection closed: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("Error occurred on connection " + (conn != null ? conn.getRemoteSocketAddress() : "null") + ":");
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        // Updated to English
        System.out.println("Server started successfully on port: " + getPort());
    }

    public static void main(String[] args) {
        int port = 8887; 
        ChatServer s = new ChatServer(port);
        s.run();
    }
}