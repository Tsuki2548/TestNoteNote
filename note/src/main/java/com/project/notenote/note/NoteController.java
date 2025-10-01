package com.project.notenote.note;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/notes")
public class NoteController {
    @Autowired
    NoteService noteService;

    public NoteDTOResponse getNotResponse(Note note){
        NoteDTOResponse response = new NoteDTOResponse(
                                                note.getNoteID(), 
                                                note.getNoteTitle()
                                            );
        return response;
    }

    public List<NoteDTOResponse> getListNoteResponse(List<Note> notes){
        List<NoteDTOResponse> response = notes.stream()
                                .map(c -> new NoteDTOResponse(
                                    c.getNoteID(),
                                    c.getNoteTitle()
                                )).toList(); 
        return response;
    }

    @GetMapping
    public ResponseEntity<List<NoteDTOResponse>> getAllNotes() {
        List<Note> notes = noteService.getAllNotes();
        List<NoteDTOResponse> response = getListNoteResponse(notes);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{noteId}") 
    public ResponseEntity<NoteDTOResponse> getNoteById(@PathVariable Long noteId) {
        Note note = noteService.getNoteById(noteId);
        NoteDTOResponse response = getNotResponse(note);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<NoteDTOResponse> createNote(@RequestBody NoteDTORequest request) {
        Note note = noteService.saveNote(request);
        NoteDTOResponse response = getNotResponse(note);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<NoteDTOResponse> UpdateNote(@PathVariable Long noteId, @RequestBody NoteDTORequest request){
        Note note = noteService.updateNote(noteId, request);
        NoteDTOResponse response = getNotResponse(note);

        return ResponseEntity.ok(response); 
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<NoteDTOResponse> deleteNote(@PathVariable Long noteId) {
        Note note = noteService.deleteNoteById(noteId);
        NoteDTOResponse response = getNotResponse(note);
        
        return ResponseEntity.ok(response);
    }

}