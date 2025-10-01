package com.project.notenote.card;

import java.util.List;

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
import com.project.notenote.label.LabelService;

@RestController
@RequestMapping("/api/cards")
public class CardController {
    @Autowired
    private CardService cardService;

    @Autowired
    private LabelService labelService;

    public CardDTOResponse getCardResponse(Card card){
        CardDTOResponse response = new CardDTOResponse(
                                            card.getCardId(),
                                            card.getCardTitle(),
                                            card.getCardContent(),
                                            card.getCardColor(),
                                            card.getBoard() != null ? card.getBoard().getBoardID() : null,
                                            card.getDate() != null ? card.getDate().getDateId() : null,
                                            card.getLabel() != null ? card.getLabel().getLabelId() : null
                                            );
        return response;
    }

    public List<CardDTOResponse> getListCardResponse(List<Card> cards){
        List<CardDTOResponse> response = cards.stream()
                                .map(c -> new CardDTOResponse(
                                    c.getCardId(),
                                    c.getCardTitle(),
                                    c.getCardContent(),
                                    c.getCardColor(),
                                    c.getBoard() != null ? c.getBoard().getBoardID() : null,
                                    c.getDate() != null ? c.getDate().getDateId() : null,
                                    c.getLabel() != null ? c.getLabel().getLabelId() : null
                                    )).toList();
        return response;
    }

    @GetMapping
    public ResponseEntity<List<CardDTOResponse>> getAllCards() {
        List<Card> cards = cardService.getAllCards();
        List<CardDTOResponse> response = getListCardResponse(cards);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<CardDTOResponse> getCardById(@PathVariable Long cardId) {
        Card card = cardService.getCardById(cardId);
        CardDTOResponse response = getCardResponse(card);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/byBoardId/{boardId}")
    public ResponseEntity<List<CardDTOResponse>> getCardsByBoardId(@PathVariable Long boardId) {
        List<Card> cards = cardService.getCardsByBoardId(boardId);
        List<CardDTOResponse> response = getListCardResponse(cards);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/byLabelId/{labelId}")
    public ResponseEntity<List<CardDTOResponse>> getCardByLabelId(@PathVariable Long labelId) {
        List<Card> cards = labelService.getCardsByLabelId(labelId);
        List<CardDTOResponse> response = getListCardResponse(cards);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CardDTOResponse> creatCard(@RequestBody CardDTORequest request) {
        Card card = cardService.saveCard(request);
        CardDTOResponse response = getCardResponse(card);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{cardId}")
    public ResponseEntity<CardDTOResponse> updateCard( @PathVariable Long cardId, @RequestBody CardDTORequest request) {
        Card card = cardService.updateCard(cardId, request);
        CardDTOResponse response = getCardResponse(card);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/reorder/{boardId}")
    public ResponseEntity<List<CardDTOResponse>> reorderCards(
        @PathVariable Long boardId,
        @RequestBody java.util.List<Long> orderedCardIds
    ){
        List<Card> after = cardService.reorderCards(boardId, orderedCardIds);
        return ResponseEntity.ok(getListCardResponse(after));
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<CardDTOResponse> deleteCard(@PathVariable Long cardId){
        Card card = cardService.daleteCardById(cardId);
        CardDTOResponse response = getCardResponse(card);
        
        return ResponseEntity.ok(response);
    }

}
