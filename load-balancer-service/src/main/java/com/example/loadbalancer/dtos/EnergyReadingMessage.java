package com.example.loadbalancer.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public class EnergyReadingMessage {

    private LocalDateTime timestamp;
    private UUID deviceId;
    private Float measurementValue;

    public EnergyReadingMessage() {}

    public EnergyReadingMessage(LocalDateTime timestamp, UUID deviceId, Float measurementValue) {
        this.timestamp = timestamp;
        this.deviceId = deviceId;
        this.measurementValue = measurementValue;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }
    public Float getMeasurementValue() { return measurementValue; }
    public void setMeasurementValue(Float measurementValue) { this.measurementValue = measurementValue; }
}

