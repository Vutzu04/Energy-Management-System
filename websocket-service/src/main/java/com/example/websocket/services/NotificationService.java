package com.example.websocket.services;

import com.example.websocket.dtos.NotificationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcast overconsumption alert to all connected clients
     */
    public void broadcastOverconsumptionAlert(
        UUID deviceId,
        String deviceName,
        Float consumption,
        Float threshold) {

        NotificationDTO notification = new NotificationDTO();
        notification.setType("OVERCONSUMPTION");
        notification.setDeviceId(deviceId);
        notification.setDeviceName(deviceName);
        notification.setConsumption(consumption);
        notification.setTimestamp(LocalDateTime.now());

        if (consumption > threshold * 1.5f) {
            notification.setSeverity("CRITICAL");
        } else if (consumption > threshold * 1.2f) {
            notification.setSeverity("HIGH");
        } else {
            notification.setSeverity("MEDIUM");
        }

        System.out.println("🚨 Broadcasting overconsumption alert: " + deviceName);
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }

    /**
     * Broadcast system notification to all connected clients
     */
    public void broadcastSystemNotification(String message) {
        NotificationDTO notification = new NotificationDTO();
        notification.setType("SYSTEM");
        notification.setMessage(message);
        notification.setTimestamp(LocalDateTime.now());
        notification.setSeverity("LOW");

        System.out.println("ℹ️  Broadcasting system notification: " + message);
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }

    /**
     * Broadcast alert notification
     */
    public void broadcastAlert(String title, String message, String severity) {
        NotificationDTO notification = new NotificationDTO();
        notification.setType("ALERT");
        notification.setMessage(title + ": " + message);
        notification.setTimestamp(LocalDateTime.now());
        notification.setSeverity(severity);

        System.out.println("⚠️  Broadcasting alert: " + title);
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }

}

