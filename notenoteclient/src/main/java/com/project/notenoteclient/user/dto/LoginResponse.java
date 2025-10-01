package com.project.notenoteclient.user.dto;

import java.util.List;

import org.springframework.http.ResponseCookie;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LoginResponse {
    private boolean success;
    private List<ResponseCookie> cookies;
    private String errorMessage;

    public boolean isSuccess() {
        return success;
    }

    public List<ResponseCookie> getCookies() {
        return cookies;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
}
