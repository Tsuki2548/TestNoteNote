package com.project.notenoteclient.user.dto;

public record LoginRequest(    
    String usernameOrEmail,
    String password
) 
{
} 
