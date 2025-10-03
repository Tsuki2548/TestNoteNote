package com.project.notenoteclient.card;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.notenoteclient.card.DTO.CardDTOResponse;
import com.project.notenoteclient.card.DTO.CardDTORequest;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/api/cards", produces = MediaType.APPLICATION_JSON_VALUE)
public class CardWebClientController {
    @Autowired
    private CardWebClientService cardService;

    @Autowired
    private com.project.notenoteclient.board.BoardWebClientService boardService;

    @Autowired
    private com.project.notenoteclient.note.NoteWebClientService noteService;

    @Autowired
    private com.project.notenoteclient.user.UsersWebClientService usersService;

    // Prevent Thymeleaf template resolution when accessing /api/cards directly
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CardDTOResponse>> getCardsRoot(){
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }

    @GetMapping(value = "/byBoardId/{boardId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CardDTOResponse>> getCardsByBoard(
        @PathVariable Long boardId,
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        // Validate the board belongs to a note owned by current user via username
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
                var board = boardService.getBoardById(boardId, cookieHeader).block();
                boolean allowed = userNotes != null && board != null && userNotes.stream().anyMatch(n -> n.getNoteId().equals(board.getNoteId()));
                if (!allowed){
                    return ResponseEntity.status(403).body(java.util.List.of());
                }
            }
        } catch (Exception ignored) {}
        List<CardDTOResponse> list = cardService.getCardByBoardId(boardId, cookieHeader).collectList().block();
        return ResponseEntity.ok(list);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CardDTOResponse> createCard(
        @RequestBody CardDTORequest request,
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) {
            return ResponseEntity.status(401).build();
        }
        // Validate the board belongs to a note owned by current user
        try {
            String accessToken = null;
            for (String cookie : cookieHeader.split(";")){
                String[] parts = cookie.trim().split("=", 2);
                if (parts.length == 2 && parts[0].equals("ACCESS_TOKEN")) { accessToken = parts[1]; break; }
            }
            if (accessToken != null && request.getBoardId() != null){
                String username = usersService.getUsername(accessToken).block();
                var userNotes = noteService.getNoteByUsername(username, cookieHeader).collectList().block();
                var board = boardService.getBoardById(request.getBoardId(), cookieHeader).block();
                boolean allowed = userNotes != null && board != null && userNotes.stream().anyMatch(n -> n.getNoteId().equals(board.getNoteId()));
                if (!allowed){
                    return ResponseEntity.status(403).build();
                }
            }
        } catch (Exception ignored) {}
        CardDTOResponse created = cardService.createCard(request, cookieHeader).block();
        return ResponseEntity.ok(created);
    }

    @DeleteMapping(value = "/{cardId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CardDTOResponse deleteCard(
        @PathVariable Long cardId,
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        return cardService.deleteCard(cardId, cookieHeader).block();
    }
    @PutMapping(value = "/{cardId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CardDTOResponse> updateCard(
        @PathVariable Long cardId,
        @RequestBody CardDTORequest request,
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) {
            return ResponseEntity.status(401).build();
        }
        // Optional: Ownership check - ensure target board (if provided) belongs to current user
        try {
            String accessToken = null;
            for (String cookie : cookieHeader.split(";")){
                String[] parts = cookie.trim().split("=", 2);
                if (parts.length == 2 && parts[0].equals("ACCESS_TOKEN")) { accessToken = parts[1]; break; }
            }
            if (accessToken != null){
                String username = usersService.getUsername(accessToken).block();
                var userNotes = noteService.getNoteByUsername(username, cookieHeader).collectList().block();
                Long boardId = request.getBoardId();
                if (boardId != null){
                    var board = boardService.getBoardById(boardId, cookieHeader).block();
                    boolean allowed = userNotes != null && board != null && userNotes.stream().anyMatch(n -> n.getNoteId().equals(board.getNoteId()));
                    if (!allowed){
                        return ResponseEntity.status(403).build();
                    }
                }
            }
        } catch (Exception ignored) {}
        CardDTOResponse updated = cardService.updateCard(cardId, request, cookieHeader).block();
        return ResponseEntity.ok(updated);
    }

    @PutMapping(value = "/reorder/{boardId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CardDTOResponse>> reorderCards(
        @PathVariable Long boardId,
        @RequestBody List<Long> orderedCardIds,
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) {
            return ResponseEntity.status(401).build();
        }
        // Ownership check: board must belong to a note of current user
        try {
            String accessToken = null;
            for (String cookie : cookieHeader.split(";")){
                String[] parts = cookie.trim().split("=", 2);
                if (parts.length == 2 && parts[0].equals("ACCESS_TOKEN")) { accessToken = parts[1]; break; }
            }
            if (accessToken != null){
                String username = usersService.getUsername(accessToken).block();
                var userNotes = noteService.getNoteByUsername(username, cookieHeader).collectList().block();
                var board = boardService.getBoardById(boardId, cookieHeader).block();
                boolean allowed = userNotes != null && board != null && userNotes.stream().anyMatch(n -> n.getNoteId().equals(board.getNoteId()));
                if (!allowed){ return ResponseEntity.status(403).build(); }
            }
        } catch (Exception ignored) {}
        var list = cardService.reorderCards(boardId, orderedCardIds, cookieHeader).collectList().block();
        return ResponseEntity.ok(list);
    }
}
