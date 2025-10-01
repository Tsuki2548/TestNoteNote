package com.project.notenote.date;

public class DateNotFoundException extends RuntimeException{
    DateNotFoundException(Long dateId){
        super("Could not find date " + dateId);
    }
}
