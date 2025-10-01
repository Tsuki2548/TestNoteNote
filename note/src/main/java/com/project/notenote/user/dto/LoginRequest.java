package com.project.notenote.user.dto;

public record LoginRequest(
    String usernameOrEmail,
    String password
) {
} 
