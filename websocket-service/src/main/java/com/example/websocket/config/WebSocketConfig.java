package com.example.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint for clients (SockJS fallback support)
        registry.addEndpoint("/ws")
            .setAllowedOrigins("http://localhost:3000", "http://localhost:5173")
            .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Simple in-memory message broker for development
        // In production, use external broker like RabbitMQ
        config.enableSimpleBroker("/topic", "/queue");
        
        // User destinations for private messages
        config.setUserDestinationPrefix("/user");
        
        // Application prefix for @SendTo and @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
    }

}

