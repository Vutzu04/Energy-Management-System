package com.example.devicemanagement.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class DeviceCreateDTO {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Maximum consumption value is required")
    @Positive(message = "Maximum consumption value must be positive")
    private Double maximumConsumptionValue;

    public DeviceCreateDTO() {
    }

    public DeviceCreateDTO(String name, Double maximumConsumptionValue) {
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

