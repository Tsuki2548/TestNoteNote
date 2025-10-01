package com.project.notenoteclient.card;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class CardWebClientConfig {
    @Bean("cardWebClient")
    public WebClient cardWebClient(WebClient.Builder builder){
        return builder
                    .baseUrl("http://localhost:8080/api/cards")
                    .build();
    }
}
