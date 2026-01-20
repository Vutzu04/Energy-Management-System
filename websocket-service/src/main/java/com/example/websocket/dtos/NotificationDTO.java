package com.example.websocket.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

public class NotificationDTO {
    
    private String type;
    private UUID deviceId;
    private String deviceName;
    private Float consumption;
    private String message;
    private LocalDateTime timestamp;
    private String severity;

    public NotificationDTO() {}

    public NotificationDTO(String type, UUID deviceId, String deviceName, Float consumption, String message, LocalDateTime timestamp, String severity) {
        this.type = type;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.consumption = consumption;
        this.message = message;
        this.timestamp = timestamp;
        this.severity = severity;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public Float getConsumption() { return consumption; }
    public void setConsumption(Float consumption) { this.consumption = consumption; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}

