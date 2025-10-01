package com.project.notenoteclient.date;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
@Configuration
public class DateWebClientConfig {

    @Bean("dateWebClient")
    public WebClient dateWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://localhost:8082/api/dates")
                .build(); 
    }
}
