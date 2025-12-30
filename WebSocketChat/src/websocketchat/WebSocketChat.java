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
import javafx.scene.paint.ImagePattern;
import javafx.scene.image.Image;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

// استيراد مكتبات الـ WebSocket
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class WebSocketChat extends Application {

    private VBox messageArea;
    private TextField inputField;
    private WebSocketClient client;
    private final String IMAGE_PATH = "/websocketchat/userimage/user.jpeg";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // 1. طلب عنوان IP السيرفر قبل فتح الواجهة
        String serverIP = askForIP();
        if (serverIP == null) {
            Platform.exit();
            return;
        }

        // 2. تصميم الواجهة الرسومية
        BorderPane root = new BorderPane();

        // الهيدر
        Label groupTitle = new Label("مجموعة البرمجة 💬");
        groupTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        HBox header = new HBox(groupTitle);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #2c3e50;");
        root.setTop(header);

        // منطقة الرسائل
        messageArea = new VBox(15);
        messageArea.setPadding(new Insets(15));
        ScrollPane scrollPane = new ScrollPane(messageArea);
        scrollPane.setFitToWidth(true);
        root.setCenter(scrollPane);

        // منطقة الإدخال
        HBox inputBox = new HBox(10);
        inputBox.setPadding(new Insets(10));
        inputField = new TextField();
        inputField.setPromptText("اكتب رسالتك هنا...");
        HBox.setHgrow(inputField, Priority.ALWAYS);
        
        Button sendButton = new Button("إرسال");
        sendButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        
        sendButton.setOnAction(e -> sendMessage());
        inputField.setOnAction(e -> sendMessage());

        inputBox.getChildren().addAll(inputField, sendButton);
        root.setBottom(inputBox);

        Scene scene = new Scene(root, 450, 600);
        stage.setTitle("Chat Client - WebSocket");
        stage.setScene(scene);
        stage.show();

        // 3. محاولة الاتصال بالسيرفر
        initWebSocket(serverIP);
    }

    private String askForIP() {
        TextInputDialog dialog = new TextInputDialog("127.0.0.1");
        dialog.setTitle("الاتصال بالشبكة");
        dialog.setHeaderText("إعدادات تطبيق المحادثة");
        dialog.setContentText("أدخل عنوان IP جهاز السيرفر:");
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private void initWebSocket(String ip) {
        try {
            client = new WebSocketClient(new URI("ws://" + ip + ":8887")) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("تم الاتصال بالسيرفر: " + ip);
                }

                @Override
                public void onMessage(String message) {
                    // استقبال الرسائل من الآخرين (باللون الأخضر)
                    Platform.runLater(() -> {
                        displayMessage("طرف آخر", message, Pos.CENTER_LEFT, Color.web("#99ff99"));
                    });
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("انقطع الاتصال بالسيرفر");
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
            client.send(text); // إرسال للسيرفر
            // عرض رسالتي أنا (باللون الأزرق)
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
            avatar.setFill(Color.web("#bdc3c7")); // لون افتراضي إذا لم تجد الصورة
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
}