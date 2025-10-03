package com.project.notenote.board;

public class BoardDTORequest {
    private String boardTitle; 
    private Long noteId;       

    public BoardDTORequest(String boardTitle, Long noteId) {
        this.boardTitle = boardTitle;
        this.noteId = noteId;
    }

    public void setBoardTitle(String boardTitle) {this.boardTitle = boardTitle;};
    public String getBoardTitle() {return boardTitle;};

    public void setNoteId(Long noteId) {this.noteId = noteId;};
    public Long getNoteId() {return noteId;};
}
