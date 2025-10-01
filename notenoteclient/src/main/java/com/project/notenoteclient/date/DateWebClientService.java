package com.project.notenoteclient.date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.project.notenoteclient.date.DTO.DateDTORequest;
import com.project.notenoteclient.date.DTO.DateDTOResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DateWebClientService {
    @Autowired
    private final WebClient dateWebClient;

    public DateWebClientService(@Qualifier("dateWebClient") WebClient dateWebClient){
        this.dateWebClient = dateWebClient;
    }

    public Flux<DateDTOResponse> getAllDate(String cookieHeader){
        return dateWebClient.get()
                            .uri("")
                            .header("Cookie", cookieHeader)
                            .retrieve()
                            .bodyToFlux(DateDTOResponse.class);
    }

    public Mono<DateDTOResponse> getDateById(Long dateId, String cookieHeader){
        return dateWebClient.get()
                            .uri("/{dateId}",dateId)
                            .header("Cookie", cookieHeader)
                            .retrieve()
                            .bodyToMono(DateDTOResponse.class);
    }

    public Mono<DateDTOResponse> getDateByCardId(Long cardId, String cookieHeader){
        return dateWebClient.get()
                            .uri("/byCardId/{cardId}",cardId)
                            .header("Cookie", cookieHeader)
                            .retrieve()
                            .bodyToMono(DateDTOResponse.class);
    }

    public Mono<DateDTOResponse> createDate(DateDTORequest request, String cookieHeader){
        return dateWebClient.post()
                            .uri("")
                            .header("Cookie", cookieHeader)
                            .body(Mono.just(request), DateDTORequest.class)
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, _ -> 
		                                Mono.error(new RuntimeException("Client error during create Date" )))
                            .onStatus(HttpStatusCode::is5xxServerError, _ -> 
		                                Mono.error(new RuntimeException("Server error during create Date")))
                            .bodyToMono(DateDTOResponse.class);
    }

    public Mono<DateDTOResponse> updateDate(Long dateId,DateDTORequest request, String cookieHeader){
        return dateWebClient.put()
                            .uri("/{dateId}",dateId)
                            .header("Cookie", cookieHeader)
                            .body(Mono.just(request), DateDTORequest.class)
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, _ -> 
		                                Mono.error(new RuntimeException("Client error during update Date" )))
                            .onStatus(HttpStatusCode::is5xxServerError, _ -> 
		                                Mono.error(new RuntimeException("Server error during update Date")))
                            .bodyToMono(DateDTOResponse.class);
    }

    public Mono<DateDTOResponse> deleteDate(Long dateId, String cookieHeader){
        return dateWebClient.delete()
                            .uri("/{dateId}",dateId)
                            .header("Cookie", cookieHeader)
                            .retrieve()
                            .bodyToMono(DateDTOResponse.class);
    }
}
