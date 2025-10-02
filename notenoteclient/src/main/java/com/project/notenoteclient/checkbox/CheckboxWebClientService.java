package com.project.notenoteclient.checkbox;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.project.notenoteclient.checkbox.DTO.CheckboxDTORequest;
import com.project.notenoteclient.checkbox.DTO.CheckboxDTOResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@Service
public class CheckboxWebClientService {
    @Autowired
    private final WebClient checkboxWebClient;

    public CheckboxWebClientService(@Qualifier("checkboxWebClient") WebClient checkboxWebClient){
        this.checkboxWebClient = checkboxWebClient;
    }

    public Flux<CheckboxDTOResponse> getAllCheckbox(String cookieHeader){
        return checkboxWebClient.get()
                                .uri("")
                                .header("Cookie", cookieHeader)
                                .retrieve()
                                .bodyToFlux(CheckboxDTOResponse.class);
    }

    public Mono<CheckboxDTOResponse> getCheckboxById(Long checkboxId, String cookieHeader){
        return checkboxWebClient.get()
                                .uri("/{checkboxId}",checkboxId)
                                .header("Cookie", cookieHeader)
                                .retrieve()
                                .bodyToMono(CheckboxDTOResponse.class);                   
    }

    public Flux<CheckboxDTOResponse> getCheckboxByChecklistId(Long checklistId, String cookieHeader){
        return checkboxWebClient.get()
                                .uri("/byChecklistId/{checklistId}",checklistId)
                                .header("Cookie", cookieHeader)
                                .retrieve()
                                .bodyToFlux(CheckboxDTOResponse.class);
    }

    public Mono<CheckboxDTOResponse> createCheckbox(CheckboxDTORequest request, String cookieHeader){
        return checkboxWebClient.post()
                                .uri("")
                                .header("Cookie", cookieHeader)
                                .body(Mono.just(request), CheckboxDTORequest.class)
                                .retrieve()
                                .onStatus(HttpStatusCode::is4xxClientError, response -> 
									Mono.error(new RuntimeException("Client error during create card" )))
                                .onStatus(HttpStatusCode::is5xxServerError, response -> 
									Mono.error(new RuntimeException("Server error during create card")))
                                .bodyToMono(CheckboxDTOResponse.class);
    }

    public Mono<CheckboxDTOResponse> updateCheckbox(Long checkboxId,CheckboxDTORequest request, String cookieHeader){
        return checkboxWebClient.put()
                                .uri("/{checkboxId}",checkboxId)
                                .header("Cookie", cookieHeader)
                                .body(Mono.just(request), CheckboxDTORequest.class)
                                .retrieve()
                                .onStatus(HttpStatusCode::is4xxClientError, response -> 
									Mono.error(new RuntimeException("Client error during create card" )))
                                .onStatus(HttpStatusCode::is5xxServerError, response -> 
									Mono.error(new RuntimeException("Server error during create card")))
                                .bodyToMono(CheckboxDTOResponse.class);
    }

    public Mono<CheckboxDTOResponse> deleteCheckbox(Long checkboxId, String cookieHeader){
        return checkboxWebClient.delete()
                                .uri("/{checkboxId}",checkboxId)
                                .header("Cookie", cookieHeader)
                                .retrieve()
                                .bodyToMono(CheckboxDTOResponse.class);
    }
}
