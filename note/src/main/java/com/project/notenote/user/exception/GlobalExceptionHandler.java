package com.project.notenote.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UsersNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UsersNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    
    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<String> handleDuplicateUser(DuplicateUserException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    //================= ^ add custom exception at bottom ^ ====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralError(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Something went wrong: " + ex.getMessage());
    }
}
