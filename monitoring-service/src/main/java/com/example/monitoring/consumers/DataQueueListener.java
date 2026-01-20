package com.example.monitoring.consumers;

import com.example.monitoring.dtos.EnergyReadingMessage;
import com.example.monitoring.services.EnergyService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DataQueueListener {
    
    @Autowired
    private EnergyService energyService;
    
    @Value("${replica.id:1}")
    private int replicaId;
    
    /**
     * Listen for energy readings from replica-specific ingest queue
     * Each replica listens to its own queue (replica_ingest_queue_X)
     */
    @RabbitListener(queues = "replica_ingest_queue_#{@environment.getProperty('replica.id', '1')}")
    public void processEnergyReading(EnergyReadingMessage message) {
        try {
            System.out.println("\n====================================================");
            System.out.println("📨 Replica " + replicaId + " received energy reading");
            System.out.println("   Device ID: " + message.getDeviceId());
            System.out.println("   Measurement: " + message.getMeasurementValue() + " kWh");
            System.out.println("   Timestamp: " + message.getTimestamp());
            System.out.println("====================================================\n");
            
            energyService.processEnergyReading(message);
            
            System.out.println("✅ Replica " + replicaId + " processed message");
        } catch (Exception e) {
            System.err.println("❌ Replica " + replicaId + " error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

