/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package websocketchat;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;

import javafx.stage.Stage;


public class WebSocketChat extends Application {

    
    public static void main(String[] args) {
       
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
       
        FlowPane root = new FlowPane();
        Scene scene = new Scene(root,100,100);
        stage.setScene(scene);
        stage.show();
    }
    
    
}
