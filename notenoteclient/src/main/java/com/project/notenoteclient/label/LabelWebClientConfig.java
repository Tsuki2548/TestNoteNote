package com.project.notenoteclient.label;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class LabelWebClientConfig {
    @Bean("labelWebClient")
    public WebClient labelWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://localhost:8082/api/labels")
                .build(); 
    }
}
