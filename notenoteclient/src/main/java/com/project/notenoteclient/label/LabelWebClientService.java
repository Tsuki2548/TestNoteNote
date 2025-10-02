package com.project.notenoteclient.label;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.project.notenoteclient.label.DTO.LabelDTORequest;
import com.project.notenoteclient.label.DTO.LabelDTOResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class LabelWebClientService {
    @Autowired
    private final WebClient labelWebClient;

    public LabelWebClientService(@Qualifier("labelWebClient") WebClient labelWebClient){
        this.labelWebClient = labelWebClient;
    }

    public Flux<LabelDTOResponse> getAllLabel(String cookieHeader){
        return labelWebClient.get()
                            .uri("")
                            .header("Cookie", cookieHeader)
                            .retrieve()
                            .bodyToFlux(LabelDTOResponse.class);
    }

    public Mono<LabelDTOResponse> getLabelById(Long labelId, String cookieHeader){
        return labelWebClient.get()
                            .uri("/{labelId}",labelId)
                            .header("Cookie", cookieHeader)
                            .retrieve()
                            .bodyToMono(LabelDTOResponse.class);
    }

    public Mono<LabelDTOResponse> createLabel(LabelDTORequest request, String cookieHeader){
        return labelWebClient.post()
                            .uri("")
                            .header("Cookie", cookieHeader)
                            .body(Mono.just(request), LabelDTORequest.class)
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, ignored -> 
                                        Mono.error(new RuntimeException("Client error during create label" )))
                            .onStatus(HttpStatusCode::is5xxServerError, ignored -> 
                                        Mono.error(new RuntimeException("Server error during create label")))
                            .bodyToMono(LabelDTOResponse.class);
    }
    
    public Mono<LabelDTOResponse> updateLabel(Long labelId,LabelDTORequest request, String cookieHeader){
        return labelWebClient.put()
                            .uri("/{labelId}",labelId)
                            .header("Cookie", cookieHeader)
                            .body(Mono.just(request), LabelDTORequest.class)
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, ignored -> 
                                        Mono.error(new RuntimeException("Client error during create label" )))
                            .onStatus(HttpStatusCode::is5xxServerError, ignored -> 
                                        Mono.error(new RuntimeException("Server error during create label")))
                            .bodyToMono(LabelDTOResponse.class);
    }

    public Mono<LabelDTOResponse> deleteLabel(Long labelId, String cookieHeader){
        return labelWebClient.delete()
                            .uri("/{labelId}",labelId)
                            .header("Cookie", cookieHeader)
                            .retrieve()
                            .bodyToMono(LabelDTOResponse.class);
    }

    // Proxy: labels of a card
    public Flux<LabelDTOResponse> getLabelsByCardId(Long cardId, String cookieHeader){
        return labelWebClient.get()
                .uri("/byCardId/{cardId}", cardId)
                .header("Cookie", cookieHeader)
                .retrieve()
                .bodyToFlux(LabelDTOResponse.class);
    }

    // Proxy: batch labels by ids (ids as CSV)
    public Flux<LabelDTOResponse> getLabelsBatch(String idsCsv, String cookieHeader){
        return labelWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/batch").queryParam("ids", idsCsv).build())
                .header("Cookie", cookieHeader)
                .retrieve()
                .bodyToFlux(LabelDTOResponse.class);
    }

    // Proxy: assign/remove/create-assign returning raw JSON
    public Mono<String> assignLabelToCard(Long cardId, Long labelId, String cookieHeader){
        return labelWebClient.post()
                .uri("/assign/{cardId}/{labelId}", cardId, labelId)
                .header("Cookie", cookieHeader)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> removeLabelFromCard(Long cardId, Long labelId, String cookieHeader){
        return labelWebClient.delete()
                .uri("/remove/{cardId}/{labelId}", cardId, labelId)
                .header("Cookie", cookieHeader)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> createAssignLabel(Long cardId, LabelDTORequest request, String cookieHeader){
        return labelWebClient.post()
                .uri("/create-assign/{cardId}", cardId)
                .header("Cookie", cookieHeader)
                .body(Mono.just(request), LabelDTORequest.class)
                .retrieve()
                .bodyToMono(String.class);
    }

    // Get labels for a specific note (user-scoped)
    public Flux<LabelDTOResponse> getLabelsByNoteId(Long noteId, String cookieHeader){
        return labelWebClient.get()
                .uri("/byNoteId/{noteId}", noteId)
                .header("Cookie", cookieHeader)
                .retrieve()
                .bodyToFlux(LabelDTOResponse.class);
    }

    public Mono<LabelDTOResponse> updateLabelInNote(Long noteId, Long labelId, LabelDTORequest request, String cookieHeader){
        return labelWebClient.put()
                .uri("/byNoteId/{noteId}/{labelId}", noteId, labelId)
                .header("Cookie", cookieHeader)
                .body(Mono.just(request), LabelDTORequest.class)
                .retrieve()
                .bodyToMono(LabelDTOResponse.class);
    }

    public Mono<Void> deleteLabelInNote(Long noteId, Long labelId, String cookieHeader){
        return labelWebClient.delete()
                .uri("/byNoteId/{noteId}/{labelId}", noteId, labelId)
                .header("Cookie", cookieHeader)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
