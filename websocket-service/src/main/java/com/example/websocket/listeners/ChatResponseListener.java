package com.example.websocket.listeners;

import com.example.websocket.controllers.ChatController;
import com.example.websocket.dtos.ChatMessageDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatResponseListener {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Listen for chat responses from Customer Support Service
     * When response comes back, forward it to the user via WebSocket
     */
    @RabbitListener(queues = "websocket_chat_queue")
    public void handleChatResponse(ChatMessageDTO message) {
        System.out.println("📨 WebSocket listener received response from customer support");
        System.out.println("   For user: " + message.getUsername());
        System.out.println("   Response: " + message.getResponse());

        // Send response to user's private queue
        String userId = message.getUserId().toString();
        messagingTemplate.convertAndSendToUser(
            userId,
            "/queue/chat",
            message
        );

        System.out.println("✅ Response sent to user via WebSocket");
    }

}

