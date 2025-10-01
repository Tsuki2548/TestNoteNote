package com.project.notenote.card;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.notenote.board.Board;
import com.project.notenote.board.BoardNotFoundExeption;
import com.project.notenote.board.BoardService;
import com.project.notenote.date.Date;
import com.project.notenote.date.DateService;
import com.project.notenote.label.Label;
import com.project.notenote.label.LabelService;

@Service
public class CardService {
    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private BoardService boardService;

    @Autowired
    private LabelService labelService;

    @Autowired
    private DateService dateService;


    public Card saveCard(CardDTORequest request) {
        Card card = new Card();
        Board board = boardService.getBoardById(request.getBoardId());
        card.setBoard(board); // set board **must** request

        card.setCardColor(request.getCardColor());
        card.setCardContent(request.getCardContent());

        if (request.getCardTitle() != null){// set card title if have request
            card.setCardTitle(request.getCardTitle());
        }else{
            card.setCardTitle("Default");
        }

        if (request.getDateId() != null){// set Date on card if have request
            Date date = dateService.getDateById(request.getDateId());
            card.setDate(date);
        }else{
            card.setDate(null);
        }

        if (request.getLabelId() != null){//set Lable on card if have request 
            Label label = labelService.getLabelById(request.getLabelId());
            card.setLabel(label);
        }else{
            card.setLabel(null);
        }

        // set orderIndex to end of list for this board
        List<Card> existing = cardRepository.findByBoardBoardIdOrderByOrderIndexAsc(board.getBoardID());
        int nextIndex = existing == null ? 0 : existing.size();
        card.setOrderIndex(nextIndex);
        return cardRepository.save(card);
    }

    public Card getCardById(Long cardId) {
        if (cardId == null) {
            return null;
        }
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("NotFound Card"));
    }

    public Card daleteCardById(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("NotFound Card"));
        cardRepository.delete(card);
        return card;
    }

    public Card updateCard(Long cardId, CardDTORequest request) {
        Card newCard = cardRepository.findById(cardId).orElseThrow(() -> new BoardNotFoundExeption(cardId));

        if (request.getCardTitle() != null ){ // update card title if have request
            newCard.setCardTitle(request.getCardTitle());
        }

        if (request.getCardColor() != null){
            newCard.setCardColor(request.getCardColor());
        }

        if (request.getCardContent() != null){
            newCard.setCardContent(request.getCardContent());
        }

        if (request.getBoardId() != null){//update board if have request
            Long oldBoardId = newCard.getBoard() != null ? newCard.getBoard().getBoardID() : null;
            Board newBoard = boardService.getBoardById(request.getBoardId());
            newCard.setBoard(newBoard);
            // if moved across boards, place at end of new board
            if (oldBoardId == null || !oldBoardId.equals(newBoard.getBoardID())){
                List<Card> existing = cardRepository.findByBoardBoardIdOrderByOrderIndexAsc(newBoard.getBoardID());
                int nextIndex = existing == null ? 0 : existing.size();
                newCard.setOrderIndex(nextIndex);
            }
        }

        if (request.getDateId() != null){// update date if have request
            Date date = dateService.getDateById(request.getDateId());
            newCard.setDate(date);
        }

        if (request.getLabelId() != null){//update label if have request
            Label label = labelService.getLabelById(request.getLabelId());
            newCard.setLabel(label);
        }

        return cardRepository.save(newCard);
    }

    public List<Card> getAllCards() {
        return (List<Card>) cardRepository.findAll();
    }

    public List<Card> getCardsByBoardId(Long boardId) {
        return cardRepository.findByBoardBoardIdOrderByOrderIndexAsc(boardId);
    }

    public List<Card> reorderCards(Long boardId, List<Long> orderedCardIds){
        // compute new orderIndex for cards in boardId following provided order
        List<Card> cards = cardRepository.findByBoardBoardIdOrderByOrderIndexAsc(boardId);
        if (cards == null) return java.util.Collections.emptyList();
        java.util.Map<Long, Card> byId = new java.util.HashMap<>();
        for (Card c : cards){ byId.put(c.getCardId(), c); }
        java.util.List<Card> result = new java.util.ArrayList<>();
        int idx = 0;
        if (orderedCardIds != null){
            for (Long id : orderedCardIds){
                Card c = byId.remove(id);
                if (c != null){ c.setOrderIndex(idx++); result.add(c); }
            }
        }
        for (Card c : byId.values()){ c.setOrderIndex(idx++); result.add(c); }
        cardRepository.saveAll(result);
        return result;
    }
    
}
