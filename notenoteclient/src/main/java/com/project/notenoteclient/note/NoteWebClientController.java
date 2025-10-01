package com.project.notenoteclient.note;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.notenoteclient.note.DTO.NoteDTORequest;
import com.project.notenoteclient.note.DTO.NoteDTOResponse;

import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/notes")
public class NoteWebClientController {
    
    @Autowired
    NoteWebClientService noteService;

    @PostMapping("/create")
    public NoteDTOResponse createLabel(
        @RequestBody NoteDTORequest request, 
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) {
            throw new RuntimeException("Access token not found. please login");
        }
        return noteService.createNote(request, cookieHeader).block();
    }

    @PutMapping("/update/{noteId}")
    public NoteDTOResponse updateLabel(
        @PathVariable Long noteId,
        @RequestBody NoteDTORequest request, 
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) {
            throw new RuntimeException("Access token not found. please login");
        }
        return noteService.updateNote(noteId, request, cookieHeader).block();
    }

    @DeleteMapping("/delete/{noteId}")
    public NoteDTOResponse deleteLabel(
        @PathVariable Long noteId, 
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) {
            throw new RuntimeException("Access token not found. please login");
        }
        return noteService.deleteNoteById(noteId, cookieHeader).block();
    }

    
}
