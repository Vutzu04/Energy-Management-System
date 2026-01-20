package com.example.monitoring.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "hourly_totals")
public class HourlyTotal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private UUID deviceId;
    
    @Column(nullable = false)
    private LocalDateTime hour;
    
    @Column(nullable = false)
    private Float totalConsumptionKwh;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public HourlyTotal() {}

    public HourlyTotal(UUID deviceId, LocalDateTime hour, Float totalConsumptionKwh) {
        this.deviceId = deviceId;
        this.hour = hour;
        this.totalConsumptionKwh = totalConsumptionKwh;
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

    public LocalDateTime getHour() {
        return hour;
    }

    public void setHour(LocalDateTime hour) {
        this.hour = hour;
    }

    public Float getTotalConsumptionKwh() {
        return totalConsumptionKwh;
    }

    public void setTotalConsumptionKwh(Float totalConsumptionKwh) {
        this.totalConsumptionKwh = totalConsumptionKwh;
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

