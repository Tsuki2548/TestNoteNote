package com.project.notenoteclient.board;

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
    private final WebClient boardWebClient;

    public BoardWebClientService(@Qualifier("boardWebClient") WebClient boardWebClient){
        this.boardWebClient = boardWebClient;
    }

    public Flux<BoardDTOResponse> getAllBoard(String cookieHeader){
        return boardWebClient.get()
                            .uri("")
                            .headers(h->{ if (cookieHeader!=null && !cookieHeader.isBlank()) h.add("Cookie", cookieHeader); })
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, cr -> 
                                Mono.error(new RuntimeException("Client error during getAllBoard")))
                            .onStatus(HttpStatusCode::is5xxServerError, cr -> 
                                Mono.error(new RuntimeException("Server error during getAllBoard")))
                            .bodyToFlux(BoardDTOResponse.class);
    }

    public Mono<BoardDTOResponse> getBoardById(Long boardId, String cookieHeader){
        return boardWebClient.get()
                            .uri("/{boardId}",boardId)
                            .headers(h->{ if (cookieHeader!=null && !cookieHeader.isBlank()) h.add("Cookie", cookieHeader); })
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, cr -> 
                                Mono.error(new RuntimeException("Client error during getBoardById")))
                            .onStatus(HttpStatusCode::is5xxServerError, cr -> 
                                Mono.error(new RuntimeException("Server error during getBoardById")))
                            .bodyToMono(BoardDTOResponse.class);
    }

    public Flux<BoardDTOResponse> getBoardByNoteId(Long noteId, String cookieHeader){
        return boardWebClient.get()
                            .uri("/byNoteId/{noteId}",noteId)
                            .headers(h->{ if (cookieHeader!=null && !cookieHeader.isBlank()) h.add("Cookie", cookieHeader); })
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, cr -> 
                                Mono.error(new RuntimeException("Client error during getBoardByNoteId")))
                            .onStatus(HttpStatusCode::is5xxServerError, cr -> 
                                Mono.error(new RuntimeException("Server error during getBoardByNoteId")))
                            .bodyToFlux(BoardDTOResponse.class);
    }

    public Mono<BoardDTOResponse> createBoard(BoardDTORequest request, String cookieHeader){
        return boardWebClient.post()
                            .uri("")
                            .body(Mono.just(request), BoardDTORequest.class)
                            .headers(h->{ if (cookieHeader!=null && !cookieHeader.isBlank()) h.add("Cookie", cookieHeader); })
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, cr -> 
		                                Mono.error(new RuntimeException("Client error during create board" )))
                            .onStatus(HttpStatusCode::is5xxServerError, cr -> 
		                                Mono.error(new RuntimeException("Server error during create board")))
                            .bodyToMono(BoardDTOResponse.class);
    }

    public Mono<BoardDTOResponse> updateBoard(Long boardId,BoardDTORequest request, String cookieHeader){
        return boardWebClient.put()
                            .uri("/{boardId}",boardId)
                            .body(Mono.just(request), BoardDTORequest.class)
                            .headers(h->{ if (cookieHeader!=null && !cookieHeader.isBlank()) h.add("Cookie", cookieHeader); })
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, cr -> 
		                                Mono.error(new RuntimeException("Client error during update board" )))
                            .onStatus(HttpStatusCode::is5xxServerError, cr -> 
		                                Mono.error(new RuntimeException("Server error during update board")))
                            .bodyToMono(BoardDTOResponse.class);
    }

    public Mono<BoardDTOResponse> deleteBoard(Long boardId, String cookieHeader){
        return boardWebClient.delete()
                            .uri("/{boardId}",boardId)
                            .headers(h->{ if (cookieHeader!=null && !cookieHeader.isBlank()) h.add("Cookie", cookieHeader); })
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, cr -> 
                                Mono.error(new RuntimeException("Client error during deleteBoard")))
                            .onStatus(HttpStatusCode::is5xxServerError, cr -> 
                                Mono.error(new RuntimeException("Server error during deleteBoard")))
                            .bodyToMono(BoardDTOResponse.class);
    }

    public Flux<BoardDTOResponse> reorderBoards(Long noteId, java.util.List<Long> orderedBoardIds, String cookieHeader){
        return boardWebClient.put()
                            .uri("/reorder/{noteId}", noteId)
                            .body(Mono.just(orderedBoardIds), java.util.List.class)
                            .headers(h->{ if (cookieHeader!=null && !cookieHeader.isBlank()) h.add("Cookie", cookieHeader); })
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, cr -> 
                                Mono.error(new RuntimeException("Client error during reorderBoards")))
                            .onStatus(HttpStatusCode::is5xxServerError, cr -> 
                                Mono.error(new RuntimeException("Server error during reorderBoards")))
                            .bodyToFlux(BoardDTOResponse.class);
    }

}
