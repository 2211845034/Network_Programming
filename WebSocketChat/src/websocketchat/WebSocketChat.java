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
    private String userName; // متغير لتخزين اسم المستخدم
    private final String IMAGE_PATH = "/websocketchat/userimage/user.jpeg";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // 1. طلب الاسم وعنوان IP
        String config = askForConfig();
        if (config == null || !config.contains("|")) {
            Platform.exit();
            return;
        }
        
        String[] parts = config.split("\\|");
        this.userName = parts[0];
        String serverIP = parts[1];

        // 2. تصميم الواجهة الرسومية
        BorderPane root = new BorderPane();
        Label groupTitle = new Label("مجموعة البرمجة 💬 (مرحباً " + userName + ")");
        groupTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        HBox header = new HBox(groupTitle);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #2c3e50;");
        root.setTop(header);

        messageArea = new VBox(15);
        messageArea.setPadding(new Insets(15));
        ScrollPane scrollPane = new ScrollPane(messageArea);
        scrollPane.setFitToWidth(true);
        root.setCenter(scrollPane);

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
        stage.setTitle("Chat Client - " + userName);
        stage.setScene(scene);
        stage.show();

        initWebSocket(serverIP);
    }

    // نافذة تطلب الاسم والـ IP معاً
    private String askForConfig() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("إعدادات الدخول");
        dialog.setHeaderText("من فضلك أدخل بياناتك للبدء");

        ButtonType loginButtonType = new ButtonType("دخول", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("اسمي هو...");
        TextField ipField = new TextField("127.0.0.1");
        ipField.setPromptText("IP السيرفر");

        grid.add(new Label("الاسم:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("IP السيرفر:"), 0, 1);
        grid.add(ipField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return nameField.getText() + "|" + ipField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private void initWebSocket(String ip) {
        try {
            client = new WebSocketClient(new URI("ws://" + ip.trim() + ":8887")) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("تم الاتصال بنجاح!");
                }

                @Override
                public void onMessage(String fullMessage) {
                    Platform.runLater(() -> {
                        // الرسالة تأتي بصيغة "الاسم: النص"
                        if (fullMessage.contains(": ")) {
                            String[] parts = fullMessage.split(": ", 2);
                            displayMessage(parts[0], parts[1], Pos.CENTER_LEFT, Color.web("#99ff99"));
                        }
                    });
                }

                @Override public void onClose(int code, String reason, boolean remote) {}
                @Override public void onError(Exception ex) { System.err.println("خطأ: " + ex.getMessage()); }
            };
            client.connect();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void sendMessage() {
        String text = inputField.getText();
        if (!text.isEmpty() && client != null && client.isOpen()) {
            // نرسل الاسم والرسالة معاً للسيرفر
            String messageToSend = userName + ": " + text;
            client.send(messageToSend);
            
            // نعرضها عندي باسم "أنا"
            displayMessage("أنا", text, Pos.CENTER_RIGHT, Color.web("#d1e8ff"));
            inputField.clear();
        }
    }

    public void displayMessage(String user, String message, Pos alignment, Color bubbleColor) {
        Circle avatar = new Circle(18);
        URL imageUrl = getClass().getResource(IMAGE_PATH);
        if (imageUrl != null) {
            avatar.setFill(new ImagePattern(new Image(imageUrl.toExternalForm(), false)));
        } else {
            avatar.setFill(Color.web("#bdc3c7"));
        }

        VBox bubble = new VBox(5);
        bubble.setPadding(new Insets(10));
        bubble.setStyle("-fx-background-color: " + toRGBCode(bubbleColor) + "; -fx-background-radius: 15;");

        Label nameLabel = new Label(user);
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