package com.example.monitoring.dtos;

import java.util.UUID;

public class DeviceSyncMessage {
    private String eventType;
    private UUID deviceId;
    private String name;
    private Double maximumConsumptionValue;

    public DeviceSyncMessage() {}

    public DeviceSyncMessage(String eventType, UUID deviceId, String name, Double maximumConsumptionValue) {
        this.eventType = eventType;
        this.deviceId = deviceId;
        this.name = name;
        this.maximumConsumptionValue = maximumConsumptionValue;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getMaximumConsumptionValue() {
        return maximumConsumptionValue;
    }

    public void setMaximumConsumptionValue(Double maximumConsumptionValue) {
        this.maximumConsumptionValue = maximumConsumptionValue;
    }

    @Override
    public String toString() {
        return "DeviceSyncMessage{" +
                "eventType='" + eventType + '\'' +
                ", deviceId=" + deviceId +
                ", name='" + name + '\'' +
                ", maximumConsumptionValue=" + maximumConsumptionValue +
                '}';
    }
}

