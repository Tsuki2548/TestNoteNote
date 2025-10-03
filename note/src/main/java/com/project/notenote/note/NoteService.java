package com.project.notenote.note;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.project.notenote.user.Users;
import com.project.notenote.user.UsersService;

@Service
public class NoteService {
    @Autowired
    NoteRepository noteRepository;

    @Autowired
    UsersService usersService;

    public Note saveNote(NoteDTORequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (request.getNoteTitle() == null || request.getNoteTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("noteTitle is required");
        }
        Note note = new Note();
        note.setNoteTitle(request.getNoteTitle().trim());
        // Attach user by username if provided
        String username = request.getUsername();
        if (username != null && !username.trim().isEmpty()) {
            Users user = usersService.getUserByUsername(username.trim());
            note.setUser(user);
        }
        return noteRepository.save(note);
    }
    public Note getNoteById(Long noteId) {
        if (noteId == null){
            return null;
        }
        return noteRepository.findById(noteId).orElseThrow(() -> new NoteNotFoundException(noteId));
    }

    public List<Note> getAllNotes() {
        return (List<Note>) noteRepository.findAll();
    }

    public Note deleteNoteById(Long noteId) {
        Note note = noteRepository.findById(noteId).orElseThrow(() -> new NoteNotFoundException(noteId));
        noteRepository.delete(note);
        return note;
    }

    public Note updateNote(Long noteId, NoteDTORequest request) {
        Note note = noteRepository.findById(noteId).orElseThrow(() -> new NoteNotFoundException(noteId));
        if (request == null) {
            return note; // nothing to update
        }
        // Update title if provided
        if (request.getNoteTitle() != null && !request.getNoteTitle().trim().isEmpty()) {
            note.setNoteTitle(request.getNoteTitle().trim());
        }
        // Update user only when username provided (keep existing otherwise)
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            Users user = usersService.getUserByUsername(request.getUsername().trim());
            note.setUser(user);
        }
        return noteRepository.save(note);
    }

    public List<Note> getNoteByUsername(String username) {
        return noteRepository.findByUserUsername(username);
    }

}
