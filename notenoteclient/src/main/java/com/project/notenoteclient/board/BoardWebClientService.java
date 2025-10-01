package com.project.notenoteclient.board;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.project.notenoteclient.board.DTO.BoardDTORequest;
import com.project.notenoteclient.board.DTO.BoardDTOResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BoardWebClientService {
    @Autowired
    private final WebClient boardWebClient;

    public BoardWebClientService(@Qualifier("boardWebClient") WebClient boardWebClient){
        this.boardWebClient = boardWebClient;
    }

    public Flux<BoardDTOResponse> getAllBoard(String cookieHeader){
        return boardWebClient.get()
                            .uri("")
                            .header("Cookie", cookieHeader)
                            .retrieve()
                            .bodyToFlux(BoardDTOResponse.class);
    }

    public Mono<BoardDTOResponse> getBoardById(Long boardId, String cookieHeader){
        return boardWebClient.get()
                            .uri("/{boardId}",boardId)
                            .header("Cookie", cookieHeader)
                            .retrieve()
                            .bodyToMono(BoardDTOResponse.class);
    }

    public Flux<BoardDTOResponse> getBoardByNoteId(Long noteId, String cookieHeader){
        return boardWebClient.get()
                            .uri("/byNoteId/{noteId}",noteId)
                            .header("Cookie", cookieHeader)
                            .retrieve()
                            .bodyToFlux(BoardDTOResponse.class);
    }

    public Mono<BoardDTOResponse> createBoard(BoardDTORequest request, String cookieHeader){
        return boardWebClient.post()
                            .uri("")
                            .body(Mono.just(request), BoardDTORequest.class)
                            .header("Cookie", cookieHeader)
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, _ -> 
		                                Mono.error(new RuntimeException("Client error during create board" )))
                            .onStatus(HttpStatusCode::is5xxServerError, _ -> 
		                                Mono.error(new RuntimeException("Server error during create board")))
                            .bodyToMono(BoardDTOResponse.class);
    }

    public Mono<BoardDTOResponse> updateBoard(Long boardId,BoardDTORequest request, String cookieHeader){
        return boardWebClient.put()
                            .uri("/{boardId}",boardId)
                            .body(Mono.just(request), BoardDTORequest.class)
                            .header("Cookie", cookieHeader)
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, _ -> 
		                                Mono.error(new RuntimeException("Client error during update board" )))
                            .onStatus(HttpStatusCode::is5xxServerError, _ -> 
		                                Mono.error(new RuntimeException("Server error during update board")))
                            .bodyToMono(BoardDTOResponse.class);
    }

    public Mono<BoardDTOResponse> deleteBoard(Long boardId, String cookieHeader){
        return boardWebClient.delete()
                            .uri("/{boardId}",boardId)
                            .header("Cookie", cookieHeader)
                            .retrieve()
                            .bodyToMono(BoardDTOResponse.class);
    }

}
