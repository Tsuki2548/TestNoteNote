package com.project.notenoteclient.user;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.project.notenoteclient.user.cookieManager.AutoCookieFilter;
import com.project.notenoteclient.user.cookieManager.TokenRefreshFilter;

@Configuration
public class UsersWebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder,
                             TokenRefreshFilter tokenRefreshFilter,
                             AutoCookieFilter autoCookieFilter) {
        return builder
            .baseUrl("http://localhost:8080")
            .filter(autoCookieFilter)    // ต้องใส่ก่อน TokenRefreshFilter
            .filter(tokenRefreshFilter)
            .build();
    }
}
