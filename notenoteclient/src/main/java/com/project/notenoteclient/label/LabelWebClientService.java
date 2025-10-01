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
                            .onStatus(HttpStatusCode::is4xxClientError, _ -> 
		                                Mono.error(new RuntimeException("Client error during create label" )))
                            .onStatus(HttpStatusCode::is5xxServerError, _ -> 
		                                Mono.error(new RuntimeException("Server error during create label")))
                            .bodyToMono(LabelDTOResponse.class);
    }
    
    public Mono<LabelDTOResponse> updateLabel(Long labelId,LabelDTORequest request, String cookieHeader){
        return labelWebClient.put()
                            .uri("/{labelId}",labelId)
                            .header("Cookie", cookieHeader)
                            .body(Mono.just(request), LabelDTORequest.class)
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, _ -> 
		                                Mono.error(new RuntimeException("Client error during create label" )))
                            .onStatus(HttpStatusCode::is5xxServerError, _ -> 
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
}
