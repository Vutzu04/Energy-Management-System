package com.example.devicemanagement.dtos;

import jakarta.validation.constraints.Positive;

public class DeviceUpdateDTO {
    private String name;
    
    @Positive(message = "Maximum consumption value must be positive")
    private Double maximumConsumptionValue;

    public DeviceUpdateDTO() {
    }

    public DeviceUpdateDTO(String name, Double maximumConsumptionValue) {
        this.name = name;
        this.maximumConsumptionValue = maximumConsumptionValue;
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

