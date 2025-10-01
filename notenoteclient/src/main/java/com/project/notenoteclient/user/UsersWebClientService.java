package com.project.notenoteclient.user;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.internal.Function;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import com.project.notenoteclient.NotenoteclientApplication;
import com.project.notenoteclient.user.dto.LoginRequest;
import com.project.notenoteclient.user.dto.LoginResponse;
import com.project.notenoteclient.user.dto.RequestResult;
import com.project.notenoteclient.user.exception.ClientErrorException;
import com.project.notenoteclient.user.exception.ForbiddenException;
import com.project.notenoteclient.user.exception.NetworkErrorException;
import com.project.notenoteclient.user.exception.RefreshTokenException;
import com.project.notenoteclient.user.exception.ServerErrorException;

import reactor.core.publisher.Mono;

@Service
public class UsersWebClientService {
    private static final Logger log = LoggerFactory.getLogger(UsersWebClientService.class);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    @Autowired
    private final WebClient webClient;

    public UsersWebClientService(WebClient webClient, NotenoteclientApplication notenoteclientApplication, UsersWebClientConfig usersWebClientConfig) {
        this.webClient = webClient;
    }

    public Mono<Users> register(Users users) {
        return webClient.post()
                .uri("/api/register")
                .header("Content-Type", "application/json")
                .body(Mono.just(users), Users.class)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> 
                    clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new ClientErrorException("Client error: " + errorBody)))
                )
                .onStatus(HttpStatusCode::is5xxServerError, serverResponse ->
                    serverResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new ServerErrorException("Server error: " + errorBody)))
                )
                .bodyToMono(Users.class)
                .timeout(REQUEST_TIMEOUT)
                .onErrorMap(ex -> new NetworkErrorException("Network or timeout error: " + ex));
    } 
    
    public Mono<LoginResponse> login(LoginRequest users) {
        return webClient.post()
                .uri("/api/login")
                .bodyValue(users)   
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        Map<String, List<ResponseCookie>> allCookies = response.cookies();
                        List<ResponseCookie> cookies = new ArrayList<>();
                        allCookies.getOrDefault("ACCESS_TOKEN", List.of())
                            .stream()
                            .findFirst()
                            .ifPresent(cookies::add);
                        allCookies.getOrDefault("REFRESH_TOKEN", List.of())
                            .stream()
                            .findFirst()
                            .ifPresent(cookies::add);

                        return Mono.just(new LoginResponse(true, cookies, null));
                    } else {
                        return response.bodyToMono(Map.class)
                            .map(body -> {
                                String errorMsg = body.getOrDefault("error", "Unknown error").toString();
                                return new LoginResponse(false, Collections.emptyList(), errorMsg);
                            });
                    }
                })
                .onErrorResume(error -> {
                    log.error("Login error: {}", error.getMessage());
                    return Mono.just(new LoginResponse(false, Collections.emptyList(), "Cannot connect to server"));
                });
    }

    public Mono<Boolean> validateToken() {
        
        return webClient.get()
            .uri("/api/validate-token")
            .retrieve()
            .bodyToMono(String.class)
            .map(response -> true)
            .timeout(REQUEST_TIMEOUT)
            .onErrorReturn(false);
    }

    private Mono<String> refreshAccessToken(String refreshToken) {
        log.debug("this is refresh token in WebClient.");
        return webClient.post()
            .uri("/api/refresh-token")
            .cookie("REFRESH_TOKEN", refreshToken) // ต้องมีการส่ง cookie ทุกครั้งที่ request
            .exchangeToMono(response -> {
                ResponseCookie cookie = response.cookies()
                    .getOrDefault("ACCESS_TOKEN", List.of())
                    .stream()
                    .findFirst()
                    .orElse(null);

                if (cookie != null) {
                    return Mono.just(cookie.getValue());
                }

                return Mono.error(new RefreshTokenException("ACCESS_TOKEN cookie not found"));
            })
            .onErrorResume(error -> {
                log.error("Error during token refresh: {}", error.getMessage(), error);
                return Mono.error(new RefreshTokenException("Failed to refresh token: "+ error.getMessage()));
            });
            
    }

    public Mono<Boolean> logout() {
        return webClient.post()
            .uri("/api/logout")
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, response -> {
                if (response.statusCode() == HttpStatus.UNAUTHORIZED) {
                    return Mono.error(new ForbiddenException("Invalid or expired token"));
                }
                return Mono.error(new ClientErrorException("Logout failed"));
            })
            .bodyToMono(Boolean.class)
            .onErrorResume(ForbiddenException.class, ex -> {
                log.error("Authentication failed during logout: {}", ex.getMessage());
                return Mono.just(false);
            })
            .onErrorResume(Exception.class, ex -> {
                log.error("Error during logout: {}", ex.getMessage());
                return Mono.just(false);
            });
    }

    public Mono<String> getUsername(String accessToken) {
        return webClient.get()
            .uri("/api/username")
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, response -> 
                response.bodyToMono(String.class)
                    .flatMap(errorBody -> Mono.error(new ClientErrorException("Client error: " + errorBody)))
            )
            .onStatus(HttpStatusCode::is5xxServerError, response -> 
                response.bodyToMono(String.class)
                    .flatMap(errorBody -> Mono.error(new ServerErrorException("Server error: " + errorBody)))
            )
            .bodyToMono(String.class);
    }
}