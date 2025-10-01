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

    public Flux<ChecklistDTOResponse> getChecklistByCardId(Long cardId){
        return checklistWebClient.get()
                                .uri("/byCardId/{cardId}",cardId)
                                .retrieve()
                                .bodyToFlux(ChecklistDTOResponse.class);
    }

    public Mono<ChecklistDTOResponse> createChecklist(ChecklistDTORequest request, String cookieHeader){
        return checklistWebClient.post()
                                .uri("")
                                .header("Cookie", cookieHeader)
                                .body(Mono.just(request),ChecklistDTORequest.class)
                                .retrieve()
                                .onStatus(HttpStatusCode::is4xxClientError, _ -> 
		                                    Mono.error(new RuntimeException("Client error during create checklist" )))
                                .onStatus(HttpStatusCode::is5xxServerError, _ -> 
		                                    Mono.error(new RuntimeException("Server error during create checklist")))
                                .bodyToMono(ChecklistDTOResponse.class);
    }

    public Mono<ChecklistDTOResponse> updateChecklist(Long checklistId,ChecklistDTORequest request){
        return checklistWebClient.put()
                                .uri("/{checklistId}",checklistId)
                                .body(Mono.just(request), ChecklistDTORequest.class)
                                .retrieve()
                                .onStatus(HttpStatusCode::is4xxClientError, _ -> 
		                                    Mono.error(new RuntimeException("Client error during update checklist" )))
                                .onStatus(HttpStatusCode::is5xxServerError, _ -> 
		                                    Mono.error(new RuntimeException("Server error during update checklist")))
                                .bodyToMono(ChecklistDTOResponse.class);
    }

    public Mono<ChecklistDTOResponse> deleteChecklist(Long checklistId){
        return checklistWebClient.delete()
                                .uri("/{checklistId}",checklistId)
                                .retrieve()
                                .bodyToMono(ChecklistDTOResponse.class);
    }
}
