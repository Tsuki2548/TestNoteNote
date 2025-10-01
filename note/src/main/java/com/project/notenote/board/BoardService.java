package com.project.notenote.board;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.notenote.note.Note;
import com.project.notenote.note.NoteService;


@Service
public class BoardService {
    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private NoteService noteService;

    public Board saveBoard(BoardDTORequest request) {
        Note note = noteService.getNoteById(request.getNoteId());

        Board board = new Board(
                            request.getBoardTitle(),
                            note);

        return boardRepository.save(board);
    }

    public Board getBoardById(Long boardId) {
        if (boardId == null){
            return null;
        }
        return boardRepository.findById(boardId).orElseThrow(() -> new BoardNotFoundExeption(boardId));
    }

    public Board deleteBoardById(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new BoardNotFoundExeption(boardId));
        boardRepository.delete(board);
        return board;
    }

    public Board updateBoard(Long boardId, BoardDTORequest request) {
        Board newBoard = boardRepository.findById(boardId).orElseThrow(() -> new BoardNotFoundExeption(boardId));

        if (request.getBoardTitle() != null){
            newBoard.setBoardTitle(request.getBoardTitle());
        }
        
        if (request.getNoteId() != null){
            Note note = noteService.getNoteById(request.getNoteId());
            newBoard.setNote(note);
        }
        ;
        return boardRepository.save(newBoard);
    }

    public List<Board> getAllBoards() {
        return (List<Board>) boardRepository.findAll();
    }

    public List<Board> getBoardsByNoteId(Long noteId) {
        Note note = noteService.getNoteById(noteId);
        return note.getBoards();
    }


}
