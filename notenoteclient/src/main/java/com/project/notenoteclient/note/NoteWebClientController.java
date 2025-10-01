package com.project.notenoteclient.note;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.notenoteclient.note.DTO.NoteDTORequest;
import com.project.notenoteclient.note.DTO.NoteDTOResponse;
import com.project.notenoteclient.user.UsersWebClientService;

import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/notes")
public class NoteWebClientController {
    
    @Autowired
    NoteWebClientService noteService;

    @Autowired
    UsersWebClientService usersService;

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createLabel(
        @RequestBody NoteDTORequest request, 
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        // If username missing from request, derive from ACCESS_TOKEN
        try {
            if (request.getUsername() == null || request.getUsername().isBlank()){
                String accessToken = null;
                if (cookieHeader != null){
                    for (String cookie : cookieHeader.split(";")){
                        String[] parts = cookie.trim().split("=", 2);
                        if (parts.length == 2 && parts[0].equals("ACCESS_TOKEN")) { accessToken = parts[1]; break; }
                    }
                }
                if (accessToken != null){
                    String uname = usersService.getUsername(accessToken).block();
                    if (uname != null && !uname.isBlank()) request.setUsername(uname);
                }
            }
        } catch (Exception e) { /* best-effort fallback only */ }
        try {
            // Forward to note service; auth cookies included if present
            NoteDTOResponse body = noteService.createNote(request, cookieHeader).block();
            return ResponseEntity.ok(body);
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException ex) {
            // Propagate downstream HTTP status with JSON body when possible
            String msg = ex.getResponseBodyAsString();
            return ResponseEntity.status(ex.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(java.util.Map.of("error", (msg==null||msg.isBlank())? ex.getMessage(): msg));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/update/{noteId}")
    public ResponseEntity<?> updateLabel(
        @PathVariable Long noteId,
        @RequestBody NoteDTORequest request, 
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) {
            return ResponseEntity.status(401)
                .contentType(MediaType.APPLICATION_JSON)
                .body(java.util.Map.of("error", "Access token not found. please login"));
        }
        // Best-effort: if username is missing, derive from ACCESS_TOKEN
        try {
            if (request.getUsername() == null || request.getUsername().isBlank()){
                String accessToken = null;
                for (String cookie : cookieHeader.split(";")){
                    String[] parts = cookie.trim().split("=", 2);
                    if (parts.length == 2 && parts[0].equals("ACCESS_TOKEN")) { accessToken = parts[1]; break; }
                }
                if (accessToken != null){
                    String uname = usersService.getUsername(accessToken).block();
                    if (uname != null && !uname.isBlank()) request.setUsername(uname);
                }
            }
        } catch (Exception ignored) {}
        try {
            NoteDTOResponse body = noteService.updateNote(noteId, request, cookieHeader).block();
            return ResponseEntity.ok(body);
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException ex){
            String msg = ex.getResponseBodyAsString();
            return ResponseEntity.status(ex.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(java.util.Map.of("error", (msg==null||msg.isBlank())? ex.getMessage(): msg));
        } catch (Exception ex){
            return ResponseEntity.internalServerError()
                .contentType(MediaType.APPLICATION_JSON)
                .body(java.util.Map.of("error", ex.getMessage()));
        }
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
