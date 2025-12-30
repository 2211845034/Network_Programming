package websocketchat;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import java.net.URL;
import javafx.scene.paint.ImagePattern;

public class WebSocketChat extends Application {

    private VBox messageArea; 
    private TextField inputField;
    
    // تأكد أن المجلد userimage موجود داخل src/websocketchat
   
    private final String IMAGE_PATH = "/websocketchat/userimage/user.jpeg";
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        BorderPane root = new BorderPane();

        // --- الهيدر (اسم الجروب) ---
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
        scrollPane.setStyle("-fx-background: #ecf0f1; -fx-background-color: transparent;");
        root.setCenter(scrollPane);

        // --- منطقة الإدخال ---
        HBox inputBox = new HBox(10);
        inputBox.setPadding(new Insets(10));
        inputField = new TextField();
        inputField.setPromptText("اكتب رسالة...");
        HBox.setHgrow(inputField, Priority.ALWAYS);
        Button sendButton = new Button("إرسال");
        sendButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        
        sendButton.setOnAction(e -> sendMessage());
        inputField.setOnAction(e -> sendMessage());

        inputBox.getChildren().addAll(inputField, sendButton);
        root.setBottom(inputBox);

        Scene scene = new Scene(root, 450, 600);
        stage.setTitle("Chat App");
        stage.setScene(scene);
        stage.show();
    }

    private void sendMessage() {
        String text = inputField.getText();
        if (!text.isEmpty()) {
            displayMessage("أنا", text, Pos.CENTER_RIGHT, Color.web("#d1e8ff"));
            inputField.clear();
        }
    }

    public void displayMessage(String userName, String message, Pos alignment, Color bubbleColor) {
    Circle avatar = new Circle(18);
    
    URL imageUrl = getClass().getResource(IMAGE_PATH);
    if (imageUrl != null) {
        // نضع القيمة false لتعطيل التحميل في الخلفية (Background loading)
        Image img = new Image(imageUrl.toExternalForm(), false);
        
        // التأكد من عدم وجود خطأ أثناء تحميل الملف نفسه
        if (!img.isError()) {
            avatar.setFill(new ImagePattern(img));
        } else {
            avatar.setFill(Color.web("#bdc3c7"));
        }
    } else {
        avatar.setFill(Color.web("#bdc3c7"));
    }

    // بقية الكود الخاص بالفقاعة (Bubble) كما هو...
    VBox bubble = new VBox(5);
    bubble.setPadding(new Insets(10));
    bubble.setStyle("-fx-background-color: " + toRGBCode(bubbleColor) + ";" +
                    "-fx-background-radius: 15;");

    Label nameLabel = new Label(userName);
    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #2980b9;");
    
    Label msgLabel = new Label(message);
    msgLabel.setWrapText(true);
    msgLabel.setMaxWidth(200);

    bubble.getChildren().addAll(nameLabel, msgLabel);

    HBox messageRow = new HBox(10);
    if (alignment == Pos.CENTER_RIGHT) {
        messageRow.getChildren().addAll(bubble, avatar);
        messageRow.setAlignment(Pos.CENTER_RIGHT);
    } else {
        messageRow.getChildren().addAll(avatar, bubble);
        messageRow.setAlignment(Pos.CENTER_LEFT);
    }

    messageArea.getChildren().add(messageRow);
}
    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}