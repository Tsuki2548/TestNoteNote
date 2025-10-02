package com.project.notenoteclient.note;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.project.notenoteclient.note.DTO.NoteDTORequest;
import com.project.notenoteclient.note.DTO.NoteDTOResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class NoteWebClientService {
    private final WebClient noteWebClient;

    public NoteWebClientService (@Qualifier("noteWebClient") WebClient noteWebClient){
        this.noteWebClient = noteWebClient;
    }

    public Flux<NoteDTOResponse> getAllNote(String cookieHeader){
        return noteWebClient.get()
                            .uri("")
                            .headers(h->{ if (cookieHeader!=null && !cookieHeader.isBlank()) h.add("Cookie", cookieHeader); })
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, cr -> 
                                Mono.error(new RuntimeException("Client error during getAllNote")))
                            .onStatus(HttpStatusCode::is5xxServerError, cr -> 
                                Mono.error(new RuntimeException("Server error during getAllNote")))
                            .bodyToFlux(NoteDTOResponse.class);
    }

    public Mono<NoteDTOResponse> getNoteById(Long noteId, String cookieHeader){
        return noteWebClient.get()
                            .uri("/{noteId}",noteId)
                            .headers(h->{ if (cookieHeader!=null && !cookieHeader.isBlank()) h.add("Cookie", cookieHeader); })
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, cr -> 
                                Mono.error(new RuntimeException("Client error during getNoteById")))
                            .onStatus(HttpStatusCode::is5xxServerError, cr -> 
                                Mono.error(new RuntimeException("Server error during getNoteById")))
                            .bodyToMono(NoteDTOResponse.class);
    }

    public Flux<NoteDTOResponse> getNoteByUsername(String username, String cookieHeader){
        return noteWebClient.get()
                            .uri("/byUsername/{username}", username)
                            .headers(h->{ if (cookieHeader!=null && !cookieHeader.isBlank()) h.add("Cookie", cookieHeader); })
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, cr -> 
                                Mono.error(new RuntimeException("Client error during getNoteByUsername")))
                            .onStatus(HttpStatusCode::is5xxServerError, cr -> 
                                Mono.error(new RuntimeException("Server error during getNoteByUsername")))
                            .bodyToFlux(NoteDTOResponse.class);
    }

    public Mono<NoteDTOResponse> createNote(NoteDTORequest request, String cookieHeader){
        return noteWebClient.post()
                            .uri("")
                            .headers(h->{ if (cookieHeader!=null && !cookieHeader.isBlank()) h.add("Cookie", cookieHeader); })
                            .body(Mono.just(request), NoteDTORequest.class)
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, cr -> 
                                Mono.error(new RuntimeException("Client error during createNote")))
                            .onStatus(HttpStatusCode::is5xxServerError, cr -> 
                                Mono.error(new RuntimeException("Server error during createNote")))
                            .bodyToMono(NoteDTOResponse.class);
    }

    public Mono<NoteDTOResponse> updateNote(Long noteId,NoteDTORequest request, String cookieHeader){
        return noteWebClient.put()
                            .uri("/{noteId}",noteId)
                            .headers(h->{ if (cookieHeader!=null && !cookieHeader.isBlank()) h.add("Cookie", cookieHeader); })
                            .body(Mono.just(request), NoteDTORequest.class)
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, cr -> 
		                            Mono.error(new RuntimeException("Client error during updateNote" )))
                            .onStatus(HttpStatusCode::is5xxServerError, cr -> 
		                            Mono.error(new RuntimeException("Server error during updateNote")))
                            .bodyToMono(NoteDTOResponse.class);
    }

    public Mono<NoteDTOResponse> deleteNoteById(Long noteId, String cookieHeader){
        return noteWebClient.delete()
                            .uri("/{noteId}",noteId)
                            .headers(h->{ if (cookieHeader!=null && !cookieHeader.isBlank()) h.add("Cookie", cookieHeader); })
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, cr -> 
                                Mono.error(new RuntimeException("Client error during deleteNoteById")))
                            .onStatus(HttpStatusCode::is5xxServerError, cr -> 
                                Mono.error(new RuntimeException("Server error during deleteNoteById")))
                            .bodyToMono(NoteDTOResponse.class);
    }

}
