package com.project.notenote.user.exception;

public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException(String username) {
        super("Duplicate username :" + username);
    }
}
