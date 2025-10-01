package com.project.notenoteclient.note.DTO;

public class NoteDTORequest {
    private String noteTitle;

    public NoteDTORequest() {}
    public NoteDTORequest(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public String getNoteTitle() { return noteTitle; }
    public void setNoteTitle(String noteTitle) { this.noteTitle = noteTitle; }
}
