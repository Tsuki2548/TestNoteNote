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
        // assign orderIndex to the end within this note
        try {
            java.util.List<Board> existing = boardRepository.findByNoteNoteIdOrderByOrderIndexAsc(note.getNoteID());
            int next = 0;
            for (Board b : existing){
                Integer oi = b.getOrderIndex();
                if (oi != null && oi >= next) next = oi + 1;
            }
            board.setOrderIndex(next);
        } catch (Exception ignored) {}

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
        // Return boards sorted by orderIndex ascending
        return boardRepository.findByNoteNoteIdOrderByOrderIndexAsc(noteId);
    }

    public List<Board> reorderBoards(Long noteId, List<Long> orderedBoardIds){
        // Load boards of note in current order
        List<Board> boards = boardRepository.findByNoteNoteIdOrderByOrderIndexAsc(noteId);
        if (boards == null || boards.isEmpty()) return java.util.Collections.emptyList();
        java.util.Map<Long, Board> byId = new java.util.HashMap<>();
        for (Board b : boards){ byId.put(b.getBoardID(), b); }

        // Compute remaining boards in their previous relative order
        java.util.Set<Long> provided = new java.util.HashSet<>(orderedBoardIds != null ? orderedBoardIds : java.util.Collections.emptyList());
        java.util.List<Long> remaining = new java.util.ArrayList<>();
        for (Board b : boards){ if (!provided.contains(b.getBoardID())) remaining.add(b.getBoardID()); }

        // Build full order: provided first, then remaining
        java.util.List<Long> fullOrder = new java.util.ArrayList<>();
        if (orderedBoardIds != null) fullOrder.addAll(orderedBoardIds);
        fullOrder.addAll(remaining);

        // Assign new consecutive orderIndex
        int idx = 0;
        for (Long id : fullOrder){
            Board b = byId.get(id);
            if (b != null){ b.setOrderIndex(idx++); }
        }
        boardRepository.saveAll(boards);
        // Return in new order
        boards.sort(java.util.Comparator.comparingInt(x -> x.getOrderIndex()==null? Integer.MAX_VALUE : x.getOrderIndex()));
        return boards;
    }


}
