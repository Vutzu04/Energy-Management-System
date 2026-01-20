package com.example.monitoring.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    // Queue names
    public static final String DATA_COLLECTION_QUEUE = "data_collection_queue";
    public static final String SYNCHRONIZATION_QUEUE = "synchronization_queue";
    
    // Exchange name
    public static final String EXCHANGE_NAME = "monitoring_exchange";
    
    // Routing keys
    public static final String DATA_ROUTING_KEY = "energy.data";
    public static final String SYNC_ROUTING_KEY = "sync.event";
    
    @Value("${replica.id:1}")
    private int replicaId;
    
    // ===== REPLICA-SPECIFIC INGEST QUEUE =====
    @Bean
    public Queue replicaIngestQueue() {
        String queueName = "replica_ingest_queue_" + replicaId;
        return new Queue(queueName, true, false, false);
    }
    
    // ===== SYNCHRONIZATION QUEUE =====
    @Bean
    public Queue synchronizationQueue() {
        return new Queue(SYNCHRONIZATION_QUEUE, true, false, false);
    }
    
    // ===== EXCHANGE =====
    @Bean
    public DirectExchange monitoringExchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }
    
    // ===== BINDINGS =====
    @Bean
    public Binding dataBinding(Queue replicaIngestQueue, DirectExchange monitoringExchange) {
        return BindingBuilder.bind(replicaIngestQueue)
                .to(monitoringExchange)
                .with(DATA_ROUTING_KEY);
    }
    
    @Bean
    public Binding syncBinding(Queue synchronizationQueue, DirectExchange monitoringExchange) {
        return BindingBuilder.bind(synchronizationQueue)
                .to(monitoringExchange)
                .with(SYNC_ROUTING_KEY);
    }
    
    // ===== MESSAGE CONVERTER =====
    @Bean
    public MessageConverter jackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

