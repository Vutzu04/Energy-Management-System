package com.example.websocket.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // Queues
    @Bean
    public Queue chatResponseQueue() {
        return new Queue("websocket_chat_queue", true);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue("websocket_notification_queue", true);
    }

    // Exchanges
    @Bean
    public TopicExchange websocketExchange() {
        return new TopicExchange("websocket_exchange", true, false);
    }

    // Bindings
    @Bean
    public Binding chatResponseBinding(Queue chatResponseQueue, TopicExchange websocketExchange) {
        return BindingBuilder.bind(chatResponseQueue)
            .to(websocketExchange)
            .with("websocket.chat");
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange websocketExchange) {
        return BindingBuilder.bind(notificationQueue)
            .to(websocketExchange)
            .with("websocket.notification");
    }

}

