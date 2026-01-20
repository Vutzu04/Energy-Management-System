package com.example.devicemanagement.entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "devices")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double maximumConsumptionValue;

    public Device() {
    }

    public Device(String name, Double maximumConsumptionValue) {
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

