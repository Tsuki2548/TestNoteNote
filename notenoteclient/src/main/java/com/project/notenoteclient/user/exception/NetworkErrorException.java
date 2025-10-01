package com.project.notenoteclient.user.exception;

public class NetworkErrorException extends RuntimeException {
    public NetworkErrorException(String message) {
        super(message);
    }
}
