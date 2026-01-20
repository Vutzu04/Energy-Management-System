package com.example.devicemanagement.dtos;

import java.util.UUID;

public class DeviceSyncMessage {
    private UUID deviceId;
    private String name;
    private Double maximumConsumptionValue;
    private String eventType; // DEVICE_CREATED, DEVICE_UPDATED, DEVICE_DELETED

    public DeviceSyncMessage() {
    }

    public DeviceSyncMessage(UUID deviceId, String name, Double maximumConsumptionValue, String eventType) {
        this.deviceId = deviceId;
        this.name = name;
        this.maximumConsumptionValue = maximumConsumptionValue;
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

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    @Override
    public String toString() {
        return "DeviceSyncMessage{" +
                "deviceId=" + deviceId +
                ", name='" + name + '\'' +
                ", maximumConsumptionValue=" + maximumConsumptionValue +
                ", eventType='" + eventType + '\'' +
                '}';
    }
}

