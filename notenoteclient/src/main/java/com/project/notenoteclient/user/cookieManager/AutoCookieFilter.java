package com.project.notenoteclient.user.cookieManager;


import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import reactor.core.publisher.Mono;

@Component
public class AutoCookieFilter implements ExchangeFilterFunction {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AutoCookieFilter.class);

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
