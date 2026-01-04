/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package chatendpoint;

/**
 *
 * @author nadayousef
 */


import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint("/chat")
public class ChatEndpoint {

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("اتصال جديد: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("رسالة مستلمة: " + message);

        // توزيع الرسالة على جميع المتصلين
        for (Session openSession : session.getOpenSessions()) {
            try {
                if (openSession.isOpen()) {
                    if (openSession.isOpen() && !openSession.getId().equals(session.getId())) {
                        openSession.getBasicRemote().sendText("مستخدم " + session.getId() + ": " + message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("انقطع الاتصال: " + session.getId());
    }

    @OnError
    public void onError(Throwable t) {
        t.printStackTrace();
    }
}