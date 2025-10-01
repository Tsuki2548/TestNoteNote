package com.project.notenote.board;

public class BoardDTOResponse {
    private Long boardId;
    private String boardTitle;
    private Long noteId;

    public BoardDTOResponse(Long boardId, String boardTitle, Long noteId) {
        this.boardId = boardId;
        this.boardTitle = boardTitle;
        this.noteId = noteId;
    }

    public Long getBoardId(){ return boardId; }
    public String getBoardTitle(){ return boardTitle; }
    public Long getNoteId(){ return noteId; }
}
