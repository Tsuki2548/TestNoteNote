package com.project.notenoteclient.checklist;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
@Configuration
public class ChecklistWebClientConfig {
    
    @Bean("checklistWebClient")
    public WebClient checklistWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://localhost:8080/api/checklists")
                .build(); 
    }
}
