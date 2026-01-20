package com.example.customersupport.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // ============ INPUT QUEUE (from WebSocket) ============
    @Bean
    public Queue chatQueue() {
        return new Queue("chat_queue", true);  // Durable
    }

    // ============ EXCHANGES ============
    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange("chat_exchange", true, false);
    }

    @Bean
    public TopicExchange websocketExchange() {
        return new TopicExchange("websocket_exchange", true, false);
    }

    // ============ BINDINGS ============
    @Bean
    public Binding chatBinding(Queue chatQueue, TopicExchange chatExchange) {
        return BindingBuilder.bind(chatQueue)
            .to(chatExchange)
            .with("chat.user");
    }

}

