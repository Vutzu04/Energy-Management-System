package com.example.authorization.dtos;

public class LoginResponse {
    private String token;
    private String role;
    private String username;
    private String userId;

    public LoginResponse() {
    }

    public LoginResponse(String token, String role, String username) {
        this.token = token;
        this.role = role;
        this.username = username;
    }

    public LoginResponse(String token, String role, String username, String userId) {
        this.token = token;
        this.role = role;
        this.username = username;
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

