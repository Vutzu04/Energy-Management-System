package com.example.devicemanagement.dtos;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class AssociationRequest {
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    private String username;  // Optional: store username for easier lookups

    public AssociationRequest() {
    }

    public AssociationRequest(String userId, String deviceId) {
        this.userId = userId;
        this.deviceId = deviceId;
    }

    public AssociationRequest(String userId, String deviceId, String username) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    // Helper methods to convert to UUID
    public UUID getUserIdAsUUID() {
        return UUID.fromString(userId);
    }

    public UUID getDeviceIdAsUUID() {
        return UUID.fromString(deviceId);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

