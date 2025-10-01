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
            Board board = boardService.getBoardById(request.getBoardId());
            newCard.setBoard(board);
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
        Board board = boardService.getBoardById(boardId);
        return board.getCards();
    }
    
}
