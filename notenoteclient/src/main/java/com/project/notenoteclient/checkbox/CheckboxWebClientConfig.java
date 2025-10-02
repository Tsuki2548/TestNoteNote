package com.project.notenoteclient.checkbox;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
@Configuration
public class CheckboxWebClientConfig {
    
    @Bean("checkboxWebClient")
    public WebClient checkboxWebClient(WebClient.Builder builder){
        return builder
                    .baseUrl("http://localhost:8082/api/checkboxes")
                    .build();
    }
}
