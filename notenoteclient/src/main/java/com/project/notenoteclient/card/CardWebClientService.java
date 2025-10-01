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
                            .headers(h->{ if (cookieHeader!=null && !cookieHeader.isBlank()) h.add("Cookie", cookieHeader); })
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, cr -> Mono.error(new RuntimeException("Client error during getAllCard")))
                            .onStatus(HttpStatusCode::is5xxServerError, cr -> Mono.error(new RuntimeException("Server error during getAllCard")))
                            .bodyToFlux(CardDTOResponse.class);
    }

    public Mono<CardDTOResponse> getCardById(Long cardId, String cookieHeader){
        return cardWebClient.get()
                            .uri("/{cardId}",cardId)
                            .headers(h->{ if (cookieHeader!=null && !cookieHeader.isBlank()) h.add("Cookie", cookieHeader); })
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, cr -> Mono.error(new RuntimeException("Client error during getCardById")))
                            .onStatus(HttpStatusCode::is5xxServerError, cr -> Mono.error(new RuntimeException("Server error during getCardById")))
                            .bodyToMono(CardDTOResponse.class);
    }

    public Flux<CardDTOResponse> getCardByBoardId(Long boardId, String cookieHeader){
        return cardWebClient.get()
                            .uri("/byBoardId/{boardId}",boardId)
                            .headers(h->{ if (cookieHeader!=null && !cookieHeader.isBlank()) h.add("Cookie", cookieHeader); })
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, cr -> Mono.error(new RuntimeException("Client error during getCardByBoardId")))
                            .onStatus(HttpStatusCode::is5xxServerError, cr -> Mono.error(new RuntimeException("Server error during getCardByBoardId")))
                            .bodyToFlux(CardDTOResponse.class);
    }

    public Flux<CardDTOResponse> reorderCards(Long boardId, java.util.List<Long> orderedIds, String cookieHeader){
        return cardWebClient.put()
                            .uri("/reorder/{boardId}", boardId)
                            .headers(h->{ if (cookieHeader!=null && !cookieHeader.isBlank()) h.add("Cookie", cookieHeader); })
                            .body(Mono.just(orderedIds), new org.springframework.core.ParameterizedTypeReference<java.util.List<Long>>() {})
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, cr -> 
                                Mono.error(new RuntimeException("Client error during reorderCards")))
                            .onStatus(HttpStatusCode::is5xxServerError, cr -> 
                                Mono.error(new RuntimeException("Server error during reorderCards")))
                            .bodyToFlux(CardDTOResponse.class);
    }

    public Flux<CardDTOResponse> getCardByLabelId(Long labelId, String cookieHeader){
        return cardWebClient.get()
                            .uri("/byLabelId/{labelId}",labelId)
                            .headers(h->{ if (cookieHeader!=null && !cookieHeader.isBlank()) h.add("Cookie", cookieHeader); })
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, cr -> Mono.error(new RuntimeException("Client error during getCardByLabelId")))
                            .onStatus(HttpStatusCode::is5xxServerError, cr -> Mono.error(new RuntimeException("Server error during getCardByLabelId")))
                            .bodyToFlux(CardDTOResponse.class);
    }

    public Mono<CardDTOResponse> createCard(CardDTORequest request, String cookieHeader){
        return cardWebClient.post()
                            .uri("")
                            .body(Mono.just(request),CardDTORequest.class)
                            .headers(h->{ if (cookieHeader!=null && !cookieHeader.isBlank()) h.add("Cookie", cookieHeader); })
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, cr -> 
		                                Mono.error(new RuntimeException("Client error during create card" )))
                            .onStatus(HttpStatusCode::is5xxServerError, cr -> 
		                                Mono.error(new RuntimeException("Server error during create card")))
                            .bodyToMono(CardDTOResponse.class);
    }

    public Mono<CardDTOResponse> updateCard(Long cardId,CardDTORequest request, String cookieHeader){
        return cardWebClient.put()
                            .uri("/{cardId}",cardId)
                            .body(Mono.just(request),CardDTORequest.class)
                            .headers(h->{ if (cookieHeader!=null && !cookieHeader.isBlank()) h.add("Cookie", cookieHeader); })
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, cr -> 
		                                Mono.error(new RuntimeException("Client error during update card" )))
                            .onStatus(HttpStatusCode::is5xxServerError, cr -> 
		                                Mono.error(new RuntimeException("Server error during update card")))
                            .bodyToMono(CardDTOResponse.class);
    }

    public Mono<CardDTOResponse> deleteCard(Long cardId, String cookieHeader){
        return cardWebClient.delete()
                            .uri("/{cardId}",cardId)
                            .headers(h->{ if (cookieHeader!=null && !cookieHeader.isBlank()) h.add("Cookie", cookieHeader); })
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, cr -> Mono.error(new RuntimeException("Client error during deleteCard")))
                            .onStatus(HttpStatusCode::is5xxServerError, cr -> Mono.error(new RuntimeException("Server error during deleteCard")))
                            .bodyToMono(CardDTOResponse.class);
    }
 
}
