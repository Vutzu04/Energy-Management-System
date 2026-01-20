package com.example.devicemanagement.dtos;

import java.util.UUID;

public class DeviceDTO {
    private UUID id;
    private String name;
    private Double maximumConsumptionValue;

    public DeviceDTO() {
    }

    public DeviceDTO(UUID id, String name, Double maximumConsumptionValue) {
        this.id = id;
        this.name = name;
        this.maximumConsumptionValue = maximumConsumptionValue;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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
}

