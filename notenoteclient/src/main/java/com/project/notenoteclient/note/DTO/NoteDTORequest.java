package com.project.notenoteclient.note.DTO;

public class NoteDTORequest {
    private String noteTitle;
    private String username;

    public NoteDTORequest() {}
    public NoteDTORequest(String noteTitle) {
        this.noteTitle = noteTitle;
    }
    public NoteDTORequest(String noteTitle, String username) {
        this.noteTitle = noteTitle;
        this.username = username;
    }

    public String getNoteTitle() { return noteTitle; }
    public void setNoteTitle(String noteTitle) { this.noteTitle = noteTitle; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
