/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package websocketchats;

/**
 *
 * @author nadayousef
 */


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
import jakarta.websocket.*;

@ClientEndpoint // تعريف الكلاس كنقطة نهاية للعميل
public class WebSocketChats extends Application {

    private VBox messageArea;
    private TextField inputField;
    private Session session; // كائن الجلسة للتحكم في الاتصال
    private final String IMAGE_PATH = "/websocketchats/userimage/user.jpeg";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // 1. طلب عنوان IP السيرفر
        String serverIP = askForIP();
        if (serverIP == null) {
            Platform.exit();
            return;
        }

        // 2. تصميم الواجهة الرسومية
        BorderPane root = new BorderPane();

        Label groupTitle = new Label("مجموعة البرمجة  ");
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
        stage.setTitle("Chat Client - JSR 356");
        stage.setScene(scene);
        stage.show();

       
        initWebSocket(serverIP);
    }

    private String askForIP() {
        TextInputDialog dialog = new TextInputDialog("127.0.0.1");
        dialog.setTitle("الاتصال بالشبكة");
        dialog.setHeaderText("إعدادات تطبيق المحادثة القياسي");
        dialog.setContentText("أدخل عنوان IP جهاز السيرفر:");
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private void initWebSocket(String ip) {
    try {
       
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        String uri = "ws://" + ip.trim() + ":8080/ChatServerWeb/chat"; 
        System.out.println("محاولة الاتصال بالرابط: " + uri);
        container.connectToServer(this, new URI(uri));
    } catch (Exception e) {
        System.err.println("فشل الاتصال: " + e.getMessage());
        e.printStackTrace(); 
    }
}

   

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("تم الاتصال بنجاح. رقم الجلسة: " + session.getId());
    }

    @OnMessage
public void onMessage(String message) {
    // استقبال الرسائل وعرضها في خيط الواجهة
    Platform.runLater(() -> {
        if (message.contains(": ")) {
            // تقسيم الرسالة إلى قسمين: الاسم والمحتوى
            String[] parts = message.split(": ", 2);
            displayMessage(parts[0], parts[1], Pos.CENTER_LEFT, Color.web("#e2f3e5"));
        } else {
            // إذا كانت رسالة نظام أو لا تحتوي على اسم
            displayMessage("طرف آخر", message, Pos.CENTER_LEFT, Color.web("#e2f3e5"));
        }
    });
}

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("تم إغلاق الاتصال: " + reason.getReasonPhrase());
    }

    @OnError
    public void onError(Throwable t) {
        System.err.println("خطأ في الشبكة: " + t.getMessage());
    }

    // --- منطق الإرسال وعرض الرسائل ---

    private void sendMessage() {
        String text = inputField.getText();
        if (!text.isEmpty() && session != null && session.isOpen()) {
            try {
               
                session.getBasicRemote().sendText(text);
                
                displayMessage("أنا", text, Pos.CENTER_RIGHT, Color.web("#d1e8ff"));
                inputField.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void displayMessage(String userName, String message, Pos alignment, Color bubbleColor) {
        
        Circle avatar = new Circle(18);
        try {
            
            URL imageUrl = getClass().getResource(IMAGE_PATH);
            if (imageUrl != null) {
                Image img = new Image(imageUrl.toExternalForm(), false);
                avatar.setFill(new ImagePattern(img));
            } else {
                avatar.setFill(Color.web("#bdc3c7")); 
            }
        } catch (Exception e) {
            avatar.setFill(Color.web("#bdc3c7"));
        }

        
        VBox bubble = new VBox(5);
        bubble.setPadding(new Insets(10));
       
        bubble.setStyle("-fx-background-color: " + toRGBCode(bubbleColor) + 
                       "; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 5);");
        //اسم المستخدم كعنوان
        Label nameLabel = new Label(userName);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #34495e;");
        //محتوي الرساله كعنوان
        Label msgLabel = new Label(message);
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(250); 

        bubble.getChildren().addAll(nameLabel, msgLabel);


        HBox messageRow = new HBox(10);
        messageRow.setAlignment(alignment);
        messageRow.setPadding(new Insets(5, 0, 5, 0));

        if (alignment == Pos.CENTER_RIGHT) {
            // إذا كنت أنا المرسل: الفقاعة أولاً ثم الصورة على اليمين
            messageRow.getChildren().addAll(bubble, avatar);
        } else {
            // إذا كان الطرف الآخر: الصورة أولاً على اليسار ثم الفقاعة
            messageRow.getChildren().addAll(avatar, bubble);
        }

      
        messageArea.getChildren().add(messageRow);
    }

    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X", (int)(color.getRed()*255), (int)(color.getGreen()*255), (int)(color.getBlue()*255));
    }
}