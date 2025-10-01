package com.project.notenoteclient.user.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String error) {
        super("Maybe this URL is blocked by Spring security: " + error);
    }
}
