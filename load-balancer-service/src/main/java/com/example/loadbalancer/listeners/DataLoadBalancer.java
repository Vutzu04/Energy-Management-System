package com.example.loadbalancer.listeners;

import com.example.loadbalancer.dtos.EnergyReadingMessage;
import com.example.loadbalancer.services.LoadDistributionService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataLoadBalancer {

    @Autowired
    private LoadDistributionService loadDistributionService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Consume messages from central data_collection_queue
     * Distribute them to replica-specific queues based on load strategy
     */
    @RabbitListener(queues = "data_collection_queue")
    public void loadBalanceEnergyData(EnergyReadingMessage message) {
        System.out.println("\n====================================================");
        System.out.println("📨 Load Balancer received energy reading");
        System.out.println("   Device ID: " + message.getDeviceId());
        System.out.println("   Measurement: " + message.getMeasurementValue() + " kWh");
        System.out.println("   Timestamp: " + message.getTimestamp());

        // 1. Decide which replica should process this
        int selectedReplica = loadDistributionService
            .selectReplica(message.getDeviceId());

        // 2. Forward to the selected replica's queue
        String replicaQueue = "replica_ingest_queue_" + selectedReplica;

        try {
            rabbitTemplate.convertAndSend(replicaQueue, message);
            System.out.println("📤 Forwarded to: " + replicaQueue);
            System.out.println("====================================================\n");
        } catch (Exception e) {
            System.err.println("❌ Error forwarding message: " + e.getMessage());
            e.printStackTrace();
        }
    }

}

