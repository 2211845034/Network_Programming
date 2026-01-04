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
    private String userName;
    private final String IMAGE_PATH = "/websocketchat/userimage/user.jpeg";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        String config = askForConfig();
        if (config == null || !config.contains("|")) {
            Platform.exit();
            return;
        }

        String[] parts = config.split("\\|");
        this.userName = parts[0];
        String serverIP = parts[1];

        BorderPane root = new BorderPane();

        // Header - English Translation
        Label groupTitle = new Label("Programming Group  (Welcome " + userName + ")");
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

        // Input Area - English Translation
        HBox inputBox = new HBox(10);
        inputBox.setPadding(new Insets(10));
        inputField = new TextField();
        inputField.setPromptText("Type your message...");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        Button sendButton = new Button("Send");
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

    private String askForConfig() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Login Settings");
        dialog.setHeaderText("Please enter your details to start");

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Your Name");
        TextField ipField = new TextField("127.0.0.1");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Server IP:"), 0, 1);
        grid.add(ipField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return nameField.getText() + "|" + ipField.getText();
            }
            return null;
        });

//        Optional<String> result = dialog.showAndWait();
//        return result.orElse(null);
        return nameField.getText();
    }

    private void initWebSocket(String ip) {
        try {
            client = new WebSocketClient(new URI("ws://" + ip.trim() + ":8887")) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Connected!");
                }

                @Override
                public void onMessage(String fullMessage) {
                    Platform.runLater(() -> {
                        if (fullMessage.contains(": ")) {
                            String[] parts = fullMessage.split(": ", 2);
                            // Light Green for others (#e2f3e5)
                            displayMessage(parts[0], parts[1], Pos.CENTER_LEFT, Color.web("#e2f3e5"));
                        }
                    });
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("Error: " + ex.getMessage());
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
            String messageToSend = userName + ": " + text;
            client.send(messageToSend);
            // Blue for me (#d1e8ff)
            displayMessage("Me", text, Pos.CENTER_RIGHT, Color.web("#d1e8ff"));
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

        // User color logic: darken the bubble color slightly for the text to make it readable but matching
        String bubbleHex = toRGBCode(bubbleColor);
        bubble.setStyle("-fx-background-color: " + bubbleHex + "; -fx-background-radius: 15;");

        Label nameLabel = new Label(user);
        // Using a darker shade of the bubble color for the name
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: " + toRGBCode(bubbleColor.darker()) + ";");

        Label msgLabel = new Label(message);
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-text-fill: #2c3e50;"); // Dark grey for text readability

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
        return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }
}
