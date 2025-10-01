package com.project.notenote.checklist;

public class ChecklistNotFoundException extends RuntimeException{
    public ChecklistNotFoundException(String message){
        super(message);
    }
}
