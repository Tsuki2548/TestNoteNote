package com.project.notenoteclient.user.dto;

public class RequestResult<T> {
    private final T body;
    private final String newAccessToken;

    public RequestResult(T body, String newAccessToken) {
        this.body = body;
        this.newAccessToken = newAccessToken;
    }

    public T getBody() {
        return body;
    }

    public String getNewAccessToken() {
        return newAccessToken;
    }
}
