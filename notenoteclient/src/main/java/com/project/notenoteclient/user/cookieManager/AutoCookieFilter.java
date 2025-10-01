package com.project.notenoteclient.user.cookieManager;


import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.server.ServerWebExchange;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AutoCookieFilter implements ExchangeFilterFunction {

    private final HttpServletRequest httpServletRequest;

    public AutoCookieFilter(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        Cookie[] cookies = httpServletRequest.getCookies();
        
        if (cookies != null) {
            ClientRequest.Builder builder = ClientRequest.from(request);
            
            // ส่งเฉพาะ ACCESS_TOKEN เท่านั้น
            for (Cookie cookie : cookies) {
                if ("ACCESS_TOKEN".equals(cookie.getName())) {
                    log.debug("Adding ACCESS_TOKEN to request");
                    builder.cookie(cookie.getName(), cookie.getValue());
                }
            }
            
            return next.exchange(builder.build());
        }
        
        log.warn("No cookies found in HttpServletRequest");
        return next.exchange(request);
    }
}
