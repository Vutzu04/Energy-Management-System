package com.example.websocket.listeners;

import com.example.websocket.services.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Listens for overconsumption alerts from the Monitoring Service
 * via RabbitMQ and broadcasts them to all connected WebSocket clients
 */
@Component
public class NotificationListener {

    @Autowired
    private NotificationService notificationService;

    /**
     * Handle overconsumption alerts from monitoring service
     * Receives from: websocket_notification_queue (via RabbitMQ)
     * Broadcasts to: /topic/notifications (WebSocket STOMP channel)
     */
    @RabbitListener(queues = "websocket_notification_queue")
    public void handleNotification(String messageJson) {
        try {
            System.out.println("📨 WebSocket received notification from RabbitMQ");
            System.out.println("   Raw JSON: " + messageJson);
            
            // Parse JSON manually
            String type = extractField(messageJson, "type");
            String deviceName = extractField(messageJson, "deviceName");
            String deviceIdStr = extractField(messageJson, "deviceId");
            String consumptionStr = extractField(messageJson, "consumption");
            String thresholdStr = extractField(messageJson, "threshold");
            String severity = extractField(messageJson, "severity");
            
            System.out.println("   Type: " + type);
            System.out.println("   Device: " + deviceName);
            System.out.println("   Consumption: " + consumptionStr + " kWh");
            System.out.println("   Severity: " + severity);
            
            if ("OVERCONSUMPTION".equals(type) && consumptionStr != null && thresholdStr != null) {
                try {
                    UUID deviceId = UUID.fromString(deviceIdStr);
                    float consumption = Float.parseFloat(consumptionStr);
                    float threshold = Float.parseFloat(thresholdStr);
                    
                    notificationService.broadcastOverconsumptionAlert(
                        deviceId,
                        deviceName,
                        consumption,
                        threshold
                    );
                } catch (NumberFormatException e) {
                    System.err.println("❌ Error parsing consumption/threshold: " + e.getMessage());
                }
            } else {
                // For other notification types, broadcast as generic alert
                notificationService.broadcastAlert(
                    type,
                    deviceName,
                    severity != null ? severity : "MEDIUM"
                );
            }
            
            System.out.println("✅ Notification broadcasted to all connected WebSocket clients");
        } catch (Exception e) {
            System.err.println("❌ Error handling notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String extractField(String json, String field) {
        try {
            String pattern = "\"" + field + "\":\"([^\"]*)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
            // Try parsing as number if quoted value not found
            pattern = "\"" + field + "\":([0-9.]+)";
            p = java.util.regex.Pattern.compile(pattern);
            m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            System.err.println("Error extracting field " + field + ": " + e.getMessage());
        }
        return null;
    }
}
