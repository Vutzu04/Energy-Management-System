package com.example.loadbalancer.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

@Configuration
public class RabbitConfig {
    
    // ============ RABBIT ADMIN ============
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    // ============ INPUT QUEUE (from Device Simulator) ============
    @Bean
    public Queue dataCollectionQueue() {
        return new Queue("data_collection_queue", true);  // Durable
    }

    // ============ OUTPUT QUEUES (for Monitoring Replicas) ============
    @Bean
    public Queue replicaIngestQueue1() {
        return new Queue("replica_ingest_queue_1", true);
    }

    @Bean
    public Queue replicaIngestQueue2() {
        return new Queue("replica_ingest_queue_2", true);
    }

    @Bean
    public Queue replicaIngestQueue3() {
        return new Queue("replica_ingest_queue_3", true);
    }

    // ============ EXCHANGES ============
    // Note: monitoring_exchange is managed by monitoring-service
    // The load balancer only needs to manage queues for routing

    // ============ RABBIT LISTENER FACTORY ============
    // Configure to be more tolerant of missing queues at startup
    @Bean
    public RabbitListenerContainerFactory<SimpleMessageListenerContainer> rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setDefaultRequeueRejected(false);
        factory.setMissingQueuesFatal(false);
        // Disable passive queue declaration - allow queues to be created dynamically
        factory.setMismatchedQueuesFatal(false);
        return factory;
    }
}

