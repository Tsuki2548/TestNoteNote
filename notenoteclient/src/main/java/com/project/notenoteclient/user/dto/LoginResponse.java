package com.project.notenoteclient.user.dto;

import java.util.List;

import org.springframework.http.ResponseCookie;

public class LoginResponse {
    private boolean success;
    private List<ResponseCookie> cookies;
    private String errorMessage;

    public LoginResponse(boolean success, List<ResponseCookie> cookies, String errorMessage) {
        this.success = success;
        this.cookies = cookies;
        this.errorMessage = errorMessage;
    }

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
