package websocketchat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import java.net.URI;
import java.net.URL;
import java.net.InetSocketAddress;

// استيراد مكتبات الـ WebSocket (يجب إضافة الـ JAR أولاً)
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

public class WebSocketChat extends Application {

    private VBox messageArea;
    private TextField inputField;
    private WebSocketClient client; // العميل
    private final String IMAGE_PATH = "/websocketchat/userimage/user.jpeg";

    public static void main(String[] args) {
        // تشغيل السيرفر في Thread منفصل قبل تشغيل الواجهة
        new Thread(() -> {
            ChatServer server = new ChatServer(8887);
            server.run();
        }).start();

        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        BorderPane root = new BorderPane();

        // --- الهيدر ---
        Label groupTitle = new Label("مجموعة البرمجة 💬");
        groupTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        HBox header = new HBox(groupTitle);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #2c3e50;");
        root.setTop(header);

        // --- منطقة الرسائل ---
        messageArea = new VBox(15);
        messageArea.setPadding(new Insets(15));
        ScrollPane scrollPane = new ScrollPane(messageArea);
        scrollPane.setFitToWidth(true);
        root.setCenter(scrollPane);

        // --- منطقة الإدخال ---
        HBox inputBox = new HBox(10);
        inputBox.setPadding(new Insets(10));
        inputField = new TextField();
        HBox.setHgrow(inputField, Priority.ALWAYS);
        Button sendButton = new Button("إرسال");
        
        sendButton.setOnAction(e -> sendMessage());
        inputField.setOnAction(e -> sendMessage());

        inputBox.getChildren().addAll(inputField, sendButton);
        root.setBottom(inputBox);

        Scene scene = new Scene(root, 450, 600);
        stage.setTitle("Chat App with WebSocket");
        stage.setScene(scene);
        stage.show();

        // الاتصال بالسيرفر بعد تشغيل الواجهة
        initWebSocket();
    }

    private void initWebSocket() {
        try {
            client = new WebSocketClient(new URI("ws://localhost:8887")) {
                @Override
                public void onOpen(org.java_websocket.handshake.ServerHandshake handshakedata) {
                    System.out.println("تم الاتصال بالسيرفر بنجاح!");
                }

                @Override
                public void onMessage(String message) {
                    Platform.runLater(() -> {
                        displayMessage("مستخدم آخر", message, Pos.CENTER_LEFT, Color.web("#ffffff"));
                    });
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("أغلق الاتصال: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("خطأ في الاتصال: " + ex.getMessage());
                }
            };
            client.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void sendMessage() {
        String text = inputField.getText();
        if (!text.isEmpty() && client != null && client.isOpen()) {
            client.send(text); // إرسال النص للسيرفر ليوزعه على الكل
            displayMessage("أنا", text, Pos.CENTER_RIGHT, Color.web("#d1e8ff"));
            inputField.clear();
        }
    }

    public void displayMessage(String userName, String message, Pos alignment, Color bubbleColor) {
        Circle avatar = new Circle(18);
        URL imageUrl = getClass().getResource(IMAGE_PATH);
        if (imageUrl != null) {
            Image img = new Image(imageUrl.toExternalForm(), false);
            avatar.setFill(new ImagePattern(img));
        } else {
            avatar.setFill(Color.web("#bdc3c7"));
        }

        VBox bubble = new VBox(5);
        bubble.setPadding(new Insets(10));
        bubble.setStyle("-fx-background-color: " + toRGBCode(bubbleColor) + "; -fx-background-radius: 15;");

        Label nameLabel = new Label(userName);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        Label msgLabel = new Label(message);
        msgLabel.setWrapText(true);

        bubble.getChildren().addAll(nameLabel, msgLabel);

        HBox messageRow = new HBox(10);
        messageRow.setAlignment(alignment);
        if (alignment == Pos.CENTER_RIGHT) {
            messageRow.getChildren().addAll(bubble, avatar);
        } else {
            messageRow.getChildren().addAll(avatar, bubble);
        }
        messageArea.getChildren().add(messageRow);
    }

    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X", (int)(color.getRed()*255), (int)(color.getGreen()*255), (int)(color.getBlue()*255));
    }

    // --- كلاس السيرفر الداخلي ---
    static class ChatServer extends WebSocketServer {
        public ChatServer(int port) { super(new InetSocketAddress(port)); }
        @Override public void onOpen(WebSocket conn, ClientHandshake handshake) {}
        @Override public void onClose(WebSocket conn, int code, String reason, boolean remote) {}
        @Override public void onStart() { System.out.println("السيرفر يعمل!"); }
        @Override public void onError(WebSocket conn, Exception ex) {}
        
        @Override
        public void onMessage(WebSocket conn, String message) {
            // توزيع الرسالة على كل المتصلين ما عدا المرسل
            for (WebSocket sock : getConnections()) {
                if (sock != conn) {
                    sock.send(message);
                }
            }
        }
    }
}