package com.example.customersupport.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

public class ChatMessageDTO {
    
    private UUID id;
    private UUID userId;
    private String username;
    private String message;
    private String response;
    
    @JsonProperty("responseType")
    private String responseType;
    
    @JsonProperty("isFromAdmin")
    private Boolean isFromAdmin;
    
    private LocalDateTime createdAt;
    private LocalDateTime responseTime;

    public ChatMessageDTO() {}

    public ChatMessageDTO(UUID id, UUID userId, String username, String message, String response, String responseType, Boolean isFromAdmin, LocalDateTime createdAt, LocalDateTime responseTime) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.message = message;
        this.response = response;
        this.responseType = responseType;
        this.isFromAdmin = isFromAdmin;
        this.createdAt = createdAt;
        this.responseTime = responseTime;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    public String getResponseType() { return responseType; }
    public void setResponseType(String responseType) { this.responseType = responseType; }
    public Boolean getIsFromAdmin() { return isFromAdmin; }
    public void setIsFromAdmin(Boolean isFromAdmin) { this.isFromAdmin = isFromAdmin; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getResponseTime() { return responseTime; }
    public void setResponseTime(LocalDateTime responseTime) { this.responseTime = responseTime; }
}

