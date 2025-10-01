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
        Note note = new Note();
        note.setNoteTitle(request.getNoteTitle());

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

        note.setNoteTitle(request.getNoteTitle());

        Users user = usersService.getUserByUsername(request.getUsername());
        note.setUser(user);

        return noteRepository.save(note);
    }

    public List<Note> getNoteByUsername(String username) {
        return noteRepository.findByUserUsername(username);
    }

}
