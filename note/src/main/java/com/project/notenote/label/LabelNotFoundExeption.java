package com.project.notenote.label;

public class LabelNotFoundExeption extends RuntimeException {
    public LabelNotFoundExeption(Long id) {
        super("Could not find label " + id);
    }
    
}
