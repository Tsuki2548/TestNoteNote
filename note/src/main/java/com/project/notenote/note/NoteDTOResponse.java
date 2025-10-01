package com.project.notenote.note;

public class NoteDTOResponse {
    private Long noteId;
    private String noteTitle;

    public NoteDTOResponse() {}
    public NoteDTOResponse(Long noteId, String noteTitle) {
        this.noteId = noteId;
        this.noteTitle = noteTitle;
    }

    public Long getNoteId() { return noteId; }
    public void setNoteId(Long noteId) { this.noteId = noteId; }

    public String getNoteTitle() { return noteTitle; }
    public void setNoteTitle(String noteTitle) { this.noteTitle = noteTitle; }
}
