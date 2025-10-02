package com.project.notenoteclient.checklist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.project.notenoteclient.checklist.DTO.ChecklistDTORequest;
import com.project.notenoteclient.checklist.DTO.ChecklistDTOResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ChecklistWebClientService {
    @Autowired
    private final WebClient checklistWebClient;

    public ChecklistWebClientService(@Qualifier("checklistWebClient") WebClient checklistWebClient){
        this.checklistWebClient = checklistWebClient;
    }

    public Flux<ChecklistDTOResponse> getAllChecklist(){
        return checklistWebClient.get()
                                .uri("")
                                .retrieve()
                                .bodyToFlux(ChecklistDTOResponse.class);
    }

    public Mono<ChecklistDTOResponse> getChecklistById(Long checklistId){
        return checklistWebClient.get()
                                .uri("/{checklistId}",checklistId)
                                .retrieve()
                                .bodyToMono(ChecklistDTOResponse.class);
    }

    public Flux<ChecklistDTOResponse> getChecklistByCardId(Long cardId, String cookieHeader){
        return checklistWebClient.get()
                                .uri("/byCardId/{cardId}",cardId)
                                .header("Cookie", cookieHeader)
                                .retrieve()
                                .bodyToFlux(ChecklistDTOResponse.class);
    }

    public Mono<ChecklistDTOResponse> createChecklist(ChecklistDTORequest request, String cookieHeader){
        return checklistWebClient.post()
                                .uri("")
                                .header("Cookie", cookieHeader)
                                .body(Mono.just(request),ChecklistDTORequest.class)
                                .retrieve()
                                .onStatus(HttpStatusCode::is4xxClientError, response -> 
		                                    Mono.error(new RuntimeException("Client error during create checklist" )))
                                .onStatus(HttpStatusCode::is5xxServerError, response -> 
		                                    Mono.error(new RuntimeException("Server error during create checklist")))
                                .bodyToMono(ChecklistDTOResponse.class);
    }

    public Mono<ChecklistDTOResponse> updateChecklist(Long checklistId,ChecklistDTORequest request, String cookieHeader){
        return checklistWebClient.put()
                                .uri("/{checklistId}",checklistId)
                                .header("Cookie", cookieHeader)
                                .body(Mono.just(request), ChecklistDTORequest.class)
                                .retrieve()
                                .onStatus(HttpStatusCode::is4xxClientError, response -> 
                                            Mono.error(new RuntimeException("Client error during update checklist" )))
                                .onStatus(HttpStatusCode::is5xxServerError, response -> 
                                            Mono.error(new RuntimeException("Server error during update checklist")))
                                .bodyToMono(ChecklistDTOResponse.class);
    }

    public Mono<ChecklistDTOResponse> deleteChecklist(Long checklistId, String cookieHeader){
        return checklistWebClient.delete()
                                .uri("/{checklistId}",checklistId)
                                .header("Cookie", cookieHeader)
                                .retrieve()
                                .onStatus(HttpStatusCode::is4xxClientError, response -> 
                                            Mono.error(new RuntimeException("Client error during delete checklist" )))
                                .onStatus(HttpStatusCode::is5xxServerError, response -> 
                                            Mono.error(new RuntimeException("Server error during delete checklist")))
                                .bodyToMono(ChecklistDTOResponse.class);
    }
}
