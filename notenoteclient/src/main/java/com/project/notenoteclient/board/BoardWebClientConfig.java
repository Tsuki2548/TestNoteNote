package com.project.notenoteclient.board;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class BoardWebClientConfig {

    @Bean("boardWebClient")
    public WebClient boardWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://localhost:8080/api/boards")
                .build(); 
    }
}
