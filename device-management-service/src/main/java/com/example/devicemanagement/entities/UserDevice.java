package com.example.devicemanagement.entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "user_devices", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "device_id"})
})
public class UserDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "username", nullable = true)
    private String username;

    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    public UserDevice() {
    }

    public UserDevice(UUID userId, Device device) {
        this.userId = userId;
        this.device = device;
    }

    public UserDevice(UUID userId, String username, Device device) {
        this.userId = userId;
        this.username = username;
        this.device = device;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }
}

