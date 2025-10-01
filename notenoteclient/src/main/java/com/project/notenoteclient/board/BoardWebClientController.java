package com.project.notenoteclient.board;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.notenoteclient.board.DTO.BoardDTORequest;
import com.project.notenoteclient.board.DTO.BoardDTOResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/boards")
public class BoardWebClientController {
    @Autowired
    private BoardWebClientService boardService;

    @Autowired
    private com.project.notenoteclient.note.NoteWebClientService noteService;

    @Autowired
    private com.project.notenoteclient.user.UsersWebClientService usersService;
    @GetMapping("/byNoteId/{noteId}")
    public ResponseEntity<java.util.List<BoardDTOResponse>> getBoardsByNoteId(
        @PathVariable Long noteId,
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        // Validate note ownership by current user via username from ACCESS_TOKEN
        try {
            String accessToken = null;
            if (cookieHeader != null){
                for (String cookie : cookieHeader.split(";")){
                    String[] parts = cookie.trim().split("=", 2);
                    if (parts.length == 2 && parts[0].equals("ACCESS_TOKEN")) { accessToken = parts[1]; break; }
                }
            }
            if (accessToken != null){
                String username = usersService.getUsername(accessToken).block();
                var userNotes = noteService.getNoteByUsername(username, cookieHeader).collectList().block();
                boolean allowed = userNotes != null && userNotes.stream().anyMatch(n -> n.getNoteId().equals(noteId));
                if (!allowed){
                    return ResponseEntity.status(403).body(java.util.List.of());
                }
            }
        } catch (Exception ignored) {}
        java.util.List<BoardDTOResponse> list = boardService.getBoardByNoteId(noteId, cookieHeader).collectList().block();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/create")
    public ResponseEntity<BoardDTOResponse> createBoard(
        @RequestBody BoardDTORequest request, 
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) {
            return ResponseEntity.status(401).build();
        }
        // Ownership check: noteId must belong to current user
        try {
            String accessToken = null;
            for (String cookie : cookieHeader.split(";")){
                String[] parts = cookie.trim().split("=", 2);
                if (parts.length == 2 && parts[0].equals("ACCESS_TOKEN")) { accessToken = parts[1]; break; }
            }
            if (accessToken != null && request.getNoteId() != null){
                String username = usersService.getUsername(accessToken).block();
                var userNotes = noteService.getNoteByUsername(username, cookieHeader).collectList().block();
                boolean allowed = userNotes != null && userNotes.stream().anyMatch(n -> n.getNoteId().equals(request.getNoteId()));
                if (!allowed){
                    return ResponseEntity.status(403).build();
                }
            }
        } catch (Exception ignored) {}
        BoardDTOResponse created = boardService.createBoard(request, cookieHeader).block();
        return ResponseEntity.ok(created);
    }

    @PutMapping("/update/{boardId}")
    public BoardDTOResponse updateBoard(
        @PathVariable Long boardId, 
        @RequestBody BoardDTORequest request, 
        HttpServletRequest servletRequest
    ){
         String cookieHeader = servletRequest.getHeader("Cookie");
         if (cookieHeader == null) {
             throw new RuntimeException("Access token not found. please login");
         }
        return boardService.updateBoard(boardId, request, cookieHeader).block();
    }

    @DeleteMapping("/delete/{boardId}")
    public BoardDTOResponse deleteBoard(
        @PathVariable Long boardId, 
        HttpServletRequest servletRequest
    ){
         String cookieHeader = servletRequest.getHeader("Cookie");
         if (cookieHeader == null) {
             throw new RuntimeException("Access token not found. please login");
         }
        // Optional: ownership check by resolving board->note->username
        return boardService.deleteBoard(boardId, cookieHeader).block();
    }

    @PutMapping("/reorder/{noteId}")
    public ResponseEntity<java.util.List<BoardDTOResponse>> reorderBoards(
        @PathVariable Long noteId,
        @RequestBody java.util.List<Long> orderedBoardIds,
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) {
            return ResponseEntity.status(401).build();
        }
        // Ownership check: noteId must belong to current user
        try {
            String accessToken = null;
            for (String cookie : cookieHeader.split(";")){
                String[] parts = cookie.trim().split("=", 2);
                if (parts.length == 2 && parts[0].equals("ACCESS_TOKEN")) { accessToken = parts[1]; break; }
            }
            if (accessToken != null && noteId != null){
                String username = usersService.getUsername(accessToken).block();
                var userNotes = noteService.getNoteByUsername(username, cookieHeader).collectList().block();
                boolean allowed = userNotes != null && userNotes.stream().anyMatch(n -> n.getNoteId().equals(noteId));
                if (!allowed){
                    return ResponseEntity.status(403).build();
                }
            }
        } catch (Exception ignored) {}
        var list = boardService.reorderBoards(noteId, orderedBoardIds, cookieHeader).collectList().block();
        return ResponseEntity.ok(list);
    }

}
