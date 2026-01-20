package com.example.websocket.controllers;

import com.example.websocket.dtos.ChatMessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/websocket")
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Health check endpoint
    @GetMapping("/health")
    public String health() {
        return "✅ WebSocket Service is running";
    }

    /**
     * When user sends chat message from UI
     * Receives message from /app/chat and forwards to Customer Support Service
     */
    @MessageMapping("/chat")
    public void handleChatMessage(@Payload ChatMessageDTO message) {
        System.out.println("📨 WebSocket received from user: " + message.getUsername());
        System.out.println("   Message: " + message.getMessage());

        // In a real scenario, this would be forwarded to customer support service via RabbitMQ
        // For now, we just acknowledge receipt
        // The response will come back via ChatResponseListener from RabbitMQ
    }

    /**
     * Send chat response to specific user (private queue)
     * Called by ChatResponseListener when customer support sends response
     */
    public void sendChatResponseToUser(String userId, ChatMessageDTO response) {
        System.out.println("✅ Sending chat response to user: " + userId);
        messagingTemplate.convertAndSendToUser(
            userId,
            "/queue/chat",
            response
        );
    }

    /**
     * Broadcast notification to all connected clients
     * Called when overconsumption alert detected
     */
    public void broadcastNotification(Object notification) {
        System.out.println("🚨 Broadcasting notification to all clients");
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }

}

