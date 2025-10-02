package com.project.notenoteclient.user.cookieManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;


import reactor.core.publisher.Mono;

@Component
public class TokenRefreshFilter implements ExchangeFilterFunction {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TokenRefreshFilter.class);

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        return next.exchange(request)
            .flatMap(response -> {
                if (response.statusCode() == HttpStatus.UNAUTHORIZED) {
                    log.debug("Received 401, attempting to refresh token");
                    
                    // ดึง refresh token เฉพาะตอนจำเป็น
                    String refreshToken = request.cookies()
                        .getFirst("REFRESH_TOKEN");
                    

                    if (refreshToken != null) {
                        return refreshAccessToken(refreshToken)
                            .flatMap(newToken -> {
                                // สร้าง request ใหม่พร้อม token ใหม่
                                ClientRequest newRequest = ClientRequest.from(request)
                                    .cookie("ACCESS_TOKEN", newToken)
                                    .build();

                                return next.exchange(newRequest)
                                    .map(newResponse -> {
                                        ResponseCookie cookie = ResponseCookie.from("ACCESS_TOKEN", newToken)
                                            .path("/")
                                            .httpOnly(true)
                                            .build();
                                            
                                        return ClientResponse.from(newResponse)
                                            .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                            .build();
                                    });
                            });
                    }
                    
                    log.warn("No refresh token available");
                    return Mono.just(response);
                }
                return Mono.just(response);
            });
    }

    private Mono<String> refreshAccessToken(String refreshToken) {
        // เรียก refresh token endpoint
        // ส่งเฉพาะ refresh token
        return Mono.empty(); // Replace with actual implementation
    }
}