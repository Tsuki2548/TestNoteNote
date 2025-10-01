package com.project.notenoteclient.card;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.notenoteclient.card.DTO.CardDTORequest;
import com.project.notenoteclient.card.DTO.CardDTOResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/cards")
public class CardWebClientController {
    @Autowired
    private CardWebClientService cardService;
    
    @PostMapping("/create")
    public CardDTOResponse createCard(
        @RequestBody CardDTORequest request, 
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) {
            throw new RuntimeException("Access token not found. please login");
        }
        return cardService.createCard(request, cookieHeader).block();
    }

    @PutMapping("/update/{cardId}")
    public CardDTOResponse updateCard(
        @PathVariable Long cardId,
        @RequestBody CardDTORequest request, 
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) {
            throw new RuntimeException("Access token not found. please login");
        }
        return cardService.updateCard(cardId, request, cookieHeader).block();
    }

    @DeleteMapping("/delete/{cardId}")
    public CardDTOResponse deleteCard(
        @PathVariable Long cardId, 
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) {
            throw new RuntimeException("Access token not found. please login");
        }
        return cardService.deleteCard(cardId, cookieHeader).block();
    }
}
