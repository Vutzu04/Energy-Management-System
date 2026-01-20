package com.example.monitoring.consumers;

import com.example.monitoring.config.RabbitMQConfig;
import com.example.monitoring.dtos.DeviceSyncMessage;
import com.example.monitoring.services.EnergyService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SyncQueueListener {
    
    @Autowired
    private EnergyService energyService;
    
    /**
     * Listen for synchronization events (device creation/updates)
     */
    @RabbitListener(queues = RabbitMQConfig.SYNCHRONIZATION_QUEUE)
    public void processSyncMessage(DeviceSyncMessage message) {
        try {
            System.out.println("✅ Received sync message: " + message);
            
            // Check if it's a device event
            if (message.getEventType() != null && message.getEventType().startsWith("DEVICE")) {
                System.out.println("Processing device sync event...");
                energyService.handleDeviceSync(message);
            } else {
                System.out.println("Ignoring non-device sync event: " + message.getEventType());
            }
        } catch (Exception e) {
            System.err.println("❌ Error processing sync message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

