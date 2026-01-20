package com.example.customersupport.listeners;

import com.example.customersupport.dtos.ChatMessageDTO;
import com.example.customersupport.services.ChatService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageListener {

    @Autowired
    private ChatService chatService;

    /**
     * Listen for chat messages from WebSocket service
     * Received via RabbitMQ from users
     */
    @RabbitListener(queues = "chat_queue")
    public void handleChatMessage(ChatMessageDTO message) {
        System.out.println("📨 Chat message listener received from: " + message.getUsername());
        
        // Process the message (rule matching + response generation)
        chatService.processChatMessage(message);
    }

}

