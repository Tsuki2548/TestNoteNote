package com.project.notenote.board;

public class BoardNotFoundExeption extends RuntimeException {
    public BoardNotFoundExeption(Long id) {
        super("Could not find board " + id);
    }
    
}
