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
import com.project.notenote.label.LabelRepository;
import com.project.notenote.label.LabelDTORequest;
import com.project.notenote.user.Users;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardService {
    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private BoardService boardService;

    @Autowired
    private LabelService labelService;
    @Autowired
    private LabelRepository labelRepository;

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

        // set labels if provided
        if (request.getLabelIds() != null && !request.getLabelIds().isEmpty()){
            java.util.Set<Label> labels = new java.util.HashSet<>();
            for (Long lid : request.getLabelIds()){
                Label l = labelService.getLabelById(lid);
                if (l != null) labels.add(l);
            }
            card.setLabels(labels);
        }

        // set orderIndex to end of list for this board
        List<Card> existing = cardRepository.findByBoardBoardIdOrderByOrderIndexAsc(board.getBoardID());
        int nextIndex = existing == null ? 0 : existing.size();
        card.setOrderIndex(nextIndex);
        Card savedCard = cardRepository.save(card);
        // รีเฟรชจาก DB เพื่อให้ labels โหลดเต็ม
        return getCardById(savedCard.getCardId());
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

        if (request.getLabelIds() != null){
            java.util.Set<Label> labels = new java.util.HashSet<>();
            for (Long lid : request.getLabelIds()){
                Label l = labelService.getLabelById(lid);
                if (l != null) labels.add(l);
            }
            newCard.setLabels(labels);
        }

        Card savedCard = cardRepository.save(newCard);
        // รีเฟรชจาก DB เพื่อให้ labels โหลดเต็ม
        return getCardById(savedCard.getCardId());
    }
    
    // Helper APIs could be used by controller for add/remove single label
    @Transactional
    public Card addLabelToCard(Long cardId, Long labelId){
        Card card = getCardById(cardId);
        Label label = labelService.getLabelById(labelId);
        // Enforce user scoping through card->board->note->user
        try {
            Users targetUser = getOwnerUser(card);
            if (targetUser != null && label != null && label.getCards()!=null && !label.getCards().isEmpty()){
                Card any = label.getCards().iterator().next();
                Users labelOwner = getOwnerUser(any);
                if (labelOwner != null && !labelOwner.getid().equals(targetUser.getid())){
                    throw new IllegalArgumentException("Label belongs to different user context");
                }
            }
        } catch (Exception ignored) {}
        card.addLabel(label);
        return cardRepository.save(card);
    }
    @Transactional
    public Card removeLabelFromCard(Long cardId, Long labelId){
        Card card = getCardById(cardId);
        Label label = labelService.getLabelById(labelId);
        card.removeLabel(label);
        return cardRepository.save(card);
    }

    @Transactional
    public Card createOrAssignLabelToCard(Long cardId, LabelDTORequest request){
    String name = request.getLabelName();
        String color = request.getColor()!=null? request.getColor(): "#6c757d";
        // try reusing existing labels by name+color; for each candidate, attempt assignment with scope check
        java.util.List<Label> candidates = labelRepository.findByLabelNameIgnoreCaseAndColor(name, color);
        for (Label l : candidates){
            try {
                return addLabelToCard(cardId, l.getLabelId());
            } catch (IllegalArgumentException ex){
                // belongs to different user scope; try next
            }
        }
        // none suitable, create a new label and assign
        Label created = labelRepository.save(new Label(name, color));
        return addLabelToCard(cardId, created.getLabelId());
    }

    private Users getOwnerUser(Card c){
        try { return c.getBoard()!=null && c.getBoard().getNote()!=null ? c.getBoard().getNote().getUser() : null; } catch(Exception e){ return null; }
    }

    public List<Card> getAllCards() {
        return (List<Card>) cardRepository.findAll();
    }

    public List<Card> getCardsByBoardId(Long boardId) {
        return cardRepository.findByBoardBoardIdOrderByOrderIndexAsc(boardId);
    }

    public List<Card> getCardsByNoteId(Long noteId) {
        // Get all boards in this note, then get all cards in those boards
        List<Board> boards = boardService.getBoardsByNoteId(noteId);
        List<Card> allCards = new java.util.ArrayList<>();
        for (Board board : boards) {
            allCards.addAll(getCardsByBoardId(board.getBoardID()));
        }
        return allCards;
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
