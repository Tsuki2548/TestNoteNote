package com.project.notenoteclient.card;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.notenoteclient.card.DTO.CardDTOResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/cards")
public class CardWebClientController {
    @Autowired
    private CardWebClientService cardService;

    @Autowired
    private com.project.notenoteclient.board.BoardWebClientService boardService;

    @Autowired
    private com.project.notenoteclient.note.NoteWebClientService noteService;

    @Autowired
    private com.project.notenoteclient.user.UsersWebClientService usersService;

    @GetMapping("/byBoardId/{boardId}")
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

    @DeleteMapping("/{cardId}")
    public CardDTOResponse deleteCard(
        @PathVariable Long cardId,
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        return cardService.deleteCard(cardId, cookieHeader).block();
    }
}
