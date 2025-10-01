package com.project.notenoteclient.card;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.project.notenoteclient.card.DTO.CardDTORequest;
import com.project.notenoteclient.card.DTO.CardDTOResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CardWebClientService {
    @Autowired
    private final WebClient cardWebClient;

    public CardWebClientService(@Qualifier("cardWebClient") WebClient cardWebClient){
        this.cardWebClient = cardWebClient;
    }

    public Flux<CardDTOResponse> getAllCard(String cookieHeader){
        return cardWebClient.get()
                            .uri("")
                            .header("Cookie", cookieHeader)
                            .retrieve()
                            .bodyToFlux(CardDTOResponse.class);
    }

    public Mono<CardDTOResponse> getCardById(Long cardId, String cookieHeader){
        return cardWebClient.get()
                            .uri("/{cardId}",cardId)
                            .header("Cookie", cookieHeader)
                            .retrieve()
                            .bodyToMono(CardDTOResponse.class);
    }

    public Flux<CardDTOResponse> getCardByBoardId(Long boardId, String cookieHeader){
        return cardWebClient.get()
                            .uri("/byBoardId/{boardId}",boardId)
                            .header("Cookie", cookieHeader)
                            .retrieve()
                            .bodyToFlux(CardDTOResponse.class);
    }

    public Flux<CardDTOResponse> getCardByLabelId(Long labelId, String cookieHeader){
        return cardWebClient.get()
                            .uri("/byLabelId/{labelId}",labelId)
                            .header("Cookie", cookieHeader)
                            .retrieve()
                            .bodyToFlux(CardDTOResponse.class);
    }

    public Mono<CardDTOResponse> createCard(CardDTORequest request, String cookieHeader){
        return cardWebClient.post()
                            .uri("")
                            .body(Mono.just(request),CardDTORequest.class)
                            .header("Cookie", cookieHeader)
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, _ -> 
		                                Mono.error(new RuntimeException("Client error during create card" )))
                            .onStatus(HttpStatusCode::is5xxServerError, _ -> 
		                                Mono.error(new RuntimeException("Server error during create card")))
                            .bodyToMono(CardDTOResponse.class);
    }

    public Mono<CardDTOResponse> updateCard(Long cardId,CardDTORequest request, String cookieHeader){
        return cardWebClient.put()
                            .uri("/{cardId}",cardId)
                            .body(Mono.just(request),CardDTORequest.class)
                            .header("Cookie", cookieHeader)
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, _ -> 
		                                Mono.error(new RuntimeException("Client error during update card" )))
                            .onStatus(HttpStatusCode::is5xxServerError, _ -> 
		                                Mono.error(new RuntimeException("Server error during update card")))
                            .bodyToMono(CardDTOResponse.class);
    }

    public Mono<CardDTOResponse> deleteCard(Long cardId, String cookieHeader){
        return cardWebClient.delete()
                            .uri("/{cardId}",cardId)
                            .header("Cookie", cookieHeader)
                            .retrieve()
                            .bodyToMono(CardDTOResponse.class);
    }
 
}
