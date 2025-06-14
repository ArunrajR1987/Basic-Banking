package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class AuthRequest {
    
    @Schema(description = "Username for authentication", example = "john.doe")
    private String username;
    
    @Schema(description = "User password", example = "password123")
    private String password;

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
}