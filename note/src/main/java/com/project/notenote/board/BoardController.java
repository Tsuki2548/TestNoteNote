package com.project.notenote.board;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;




@RestController
@RequestMapping("/api/boards")
public class BoardController {
    @Autowired
    private BoardService boardService;

    public BoardDTOResponse getBoardResponse(Board board){
        BoardDTOResponse response = new BoardDTOResponse(
                                            board.getBoardID(),
                                            board.getBoardTitle(),
                                            board.getNote().getNoteID()
                                            );
        return response;
    }

    public List<BoardDTOResponse> getListBoardResponse(List<Board> boards){
        List<BoardDTOResponse> response =  boards.stream()
                                .map(c -> new BoardDTOResponse(
                                    c.getBoardID(),
                                    c.getBoardTitle(),
                                    c.getNote().getNoteID()
                                )).toList();
        return response;
    }

    @GetMapping
    public ResponseEntity<List<BoardDTOResponse>> getAllBoards() {
        List<Board> boards =  boardService.getAllBoards();
        List<BoardDTOResponse> response = getListBoardResponse(boards);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{boardID}")
    public ResponseEntity<BoardDTOResponse> getBoardById(@PathVariable Long boardID) {
        Board board = boardService.getBoardById(boardID);
        BoardDTOResponse response = getBoardResponse(board);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/byNoteId/{noteID}")
    public ResponseEntity<List<BoardDTOResponse>> getBoardsByNoteId(@PathVariable Long noteID) {
        List<Board> boards = boardService.getBoardsByNoteId(noteID);
        List<BoardDTOResponse> response = getListBoardResponse(boards);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<BoardDTOResponse> createBoard(@RequestBody BoardDTORequest request) {
        Board board = boardService.saveBoard(request);
        BoardDTOResponse response = getBoardResponse(board);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{boardID}")
    public ResponseEntity<BoardDTOResponse> updateBoard(@PathVariable Long boardID, @RequestBody BoardDTORequest boardRequest) {
        Board newBoard = boardService.updateBoard(boardID, boardRequest);
        BoardDTOResponse response = getBoardResponse(newBoard);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{boardID}")
    public ResponseEntity<BoardDTOResponse> deleteBoard(@PathVariable Long boardID) {
        Board board = boardService.deleteBoardById(boardID);
        BoardDTOResponse response = getBoardResponse(board);

        return ResponseEntity.ok(response);
    }

    // Reorder boards in a note: body = array of boardIds in new order
    @PutMapping("/reorder/{noteID}")
    public ResponseEntity<java.util.List<BoardDTOResponse>> reorderBoards(
        @PathVariable Long noteID,
        @org.springframework.web.bind.annotation.RequestBody java.util.List<Long> orderedBoardIds
    ){
        java.util.List<Board> boards = boardService.reorderBoards(noteID, orderedBoardIds);
        return ResponseEntity.ok(getListBoardResponse(boards));
    }
    
}
