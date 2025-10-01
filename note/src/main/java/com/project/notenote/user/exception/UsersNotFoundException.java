package com.project.notenote.user.exception;

public class UsersNotFoundException extends RuntimeException {
    public UsersNotFoundException(long id) {
        super("Could not found user :" + id);
    }
}
