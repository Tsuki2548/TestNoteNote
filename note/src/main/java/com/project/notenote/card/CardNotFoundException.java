package com.project.notenote.card;

public class CardNotFoundException extends RuntimeException{
    public CardNotFoundException(String message){
        super(message);
    }
}
