package com.example.monitoring.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "measurements")
public class Measurement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private UUID deviceId;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(nullable = false)
    private Float energyConsumptionKwh;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Measurement() {}

    public Measurement(UUID deviceId, LocalDateTime timestamp, Float energyConsumptionKwh) {
        this.deviceId = deviceId;
        this.timestamp = timestamp;
        this.energyConsumptionKwh = energyConsumptionKwh;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Float getEnergyConsumptionKwh() {
        return energyConsumptionKwh;
    }

    public void setEnergyConsumptionKwh(Float energyConsumptionKwh) {
        this.energyConsumptionKwh = energyConsumptionKwh;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

