package com.example.usermanagement.dtos;

import jakarta.validation.constraints.NotBlank;

public class UserCreateDTO {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    private String role; // Optional role for admin-create endpoint

    public UserCreateDTO() {
    }

    public UserCreateDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public UserCreateDTO(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

