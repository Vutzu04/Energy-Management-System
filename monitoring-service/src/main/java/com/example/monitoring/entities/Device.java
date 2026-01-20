package com.example.monitoring.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "devices")
public class Device {
    
    @Id
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    @Column
    private Double maximumConsumptionValue;
    
    @Column(name = "synced_at", updatable = false)
    private LocalDateTime syncedAt;

    public Device() {}

    public Device(UUID id, String name, Double maximumConsumptionValue) {
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

    public LocalDateTime getSyncedAt() {
        return syncedAt;
    }

    public void setSyncedAt(LocalDateTime syncedAt) {
        this.syncedAt = syncedAt;
    }
    
    @PrePersist
    protected void onCreate() {
        if (syncedAt == null) {
            syncedAt = LocalDateTime.now();
        }
    }
}

