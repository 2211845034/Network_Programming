/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package websocketchat;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


public class WebSocketChat extends Application {

    private VBox messageArea; // منطقة عرض الرسائل
    private TextField inputField; // حقل الكتابة
    
    public static void main(String[] args) {
       
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
           // ===== Title =====
   // 1. الإطار الرئيسي للمشروع
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // 2. منطقة عرض الرسائل (Scrollable)
        messageArea = new VBox(10);
        messageArea.setPadding(new Insets(10));
        ScrollPane scrollPane = new ScrollPane(messageArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        root.setCenter(scrollPane);

        // 3. منطقة الإدخال (أسفل الواجهة)
        HBox inputBox = new HBox(10);
        inputBox.setPadding(new Insets(10, 0, 0, 0));
        
        inputField = new TextField();
        inputField.setPromptText("اكتب رسالتك هنا...");
        HBox.setHgrow(inputField, Priority.ALWAYS); // جعل الحقل يتمدد

        Button sendButton = new Button("send");
        sendButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        
        // حدث عند الضغط على زر الإرسال
        sendButton.setOnAction(e -> sendMessage());
        inputField.setOnAction(e -> sendMessage()); // الإرسال عند الضغط على Enter

        inputBox.getChildren().addAll(inputField, sendButton);
        root.setBottom(inputBox);

        // 4. إعدادات النافذة
        Scene scene = new Scene(root, 400, 500);
        stage.setTitle("تطبيق محادثة WebSocket - Network Programming");
        stage.setScene(scene);
        stage.show();
    }

    private void sendMessage() {
        String text = inputField.getText();
        if (!text.isEmpty()) {
            // هنا يتم استدعاء وظيفة الـ WebSocket لإرسال الرسالة للسيرفر
            displayMessage( text, Pos.CENTER_RIGHT, Color.LIGHTBLUE);
            inputField.clear();
            
            // محاكاة استلام رد من السيرفر (للتجربة فقط)
            // displayMessage("السيرفر: تم الاستلام", Pos.CENTER_LEFT, Color.LIGHTGREY);
        }
    }

    // وظيفة لإضافة فقاعات المحادثة
    public void displayMessage(String message, Pos alignment, Color color) {
        Label label = new Label(message);
        label.setPadding(new Insets(8));
        label.setWrapText(true);
        label.setMaxWidth(250);
        label.setStyle("-fx-background-radius: 10; -fx-background-color: " + toRGBCode(color) + ";");

        HBox container = new HBox(label);
        container.setAlignment(alignment);
        messageArea.getChildren().add(container);
    }

    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
    
    
}
