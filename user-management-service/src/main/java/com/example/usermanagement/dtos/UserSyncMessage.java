package com.example.usermanagement.dtos;

import java.util.UUID;

public class UserSyncMessage {
    private UUID userId;
    private String username;
    private String role;
    private String eventType; // USER_CREATED, USER_UPDATED, USER_DELETED

    public UserSyncMessage() {
    }

    public UserSyncMessage(UUID userId, String username, String role, String eventType) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.eventType = eventType;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    @Override
    public String toString() {
        return "UserSyncMessage{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", eventType='" + eventType + '\'' +
                '}';
    }
}

