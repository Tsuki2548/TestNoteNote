package com.project.notenoteclient.note;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class NoteWebClientConfig {

    @Bean("noteWebClient")
    public WebClient noteWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://localhost:8082/api/notes")
                .build(); 
    }

}
