package com.project.notenote.user.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtils jwtUtils;
    private final UsersDetailsService usersDetailsService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UsersDetailsService usersDetailsService) {
        this.jwtUtils = jwtUtils;
        this.usersDetailsService = usersDetailsService;
    }

    @Override
protected void doFilterInternal(
    HttpServletRequest request, 
    HttpServletResponse response, 
    FilterChain filterChain)
        throws ServletException, IOException {

    // Skip JWT processing for public endpoints and preflight
    String path = request.getRequestURI();
    if ("OPTIONS".equalsIgnoreCase(request.getMethod()) ||
        path.startsWith("/api/login") ||
        path.startsWith("/api/register") ||
        path.startsWith("/api/validate-token") ||
        path.startsWith("/api/logout") ||
        path.startsWith("/api/refresh-token")) {
        filterChain.doFilter(request, response);
        return;
    }

    String accessToken = null;
    String refreshToken = null;

    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        accessToken = authHeader.substring(7);
        log.debug("JWT Filter: Found accessToken in header");
    }

    if (accessToken == null) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("ACCESS_TOKEN".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                } else if ("REFRESH_TOKEN".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }
    }

    if (accessToken != null && jwtUtils.validateAccessToken(accessToken)) {
        log.debug("JWT Filter: accessToken valid, authenticating user...");
        try { authenticateUser(accessToken, true, request); } catch (org.springframework.security.core.userdetails.UsernameNotFoundException ex) { log.debug("JWT Filter: user not found for token; continuing without auth"); }
    } else if (refreshToken != null && jwtUtils.validateRefreshToken(refreshToken)) {
        log.debug("JWT Filter: refreshing accessToken...");
        String newAccessToken = jwtUtils.refreshAccessToken(refreshToken);

        Cookie newAccessCookie = new Cookie("ACCESS_TOKEN", newAccessToken);
        newAccessCookie.setHttpOnly(true);
        newAccessCookie.setSecure(false);
        newAccessCookie.setPath("/");
        response.addCookie(newAccessCookie);

        try { authenticateUser(newAccessToken, true, request); } catch (org.springframework.security.core.userdetails.UsernameNotFoundException ex) { log.debug("JWT Filter: user not found after refresh; continuing without auth"); }
    } else {
        log.debug("JWT Filter: No valid token found");
    }

    filterChain.doFilter(request, response);
}

        // if (cookies != null) {
        //     for (Cookie cookie : cookies) {
        //         if ("ACCESS_TOKEN".equals(cookie.getName())) {
        //             String token = cookie.getValue();
        //             if (token != null && !token.isEmpty()) {   // <-- check
        //                 String username = jwtUtils.extractUsername(token);
        //                 if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        //                     UserDetails userDetails = usersDetailsService.loadUserByUsername(username);
        //                     if (jwtUtils.validateToken(token, userDetails.getUsername())) {
        //                         UsernamePasswordAuthenticationToken authToken =
        //                             new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        //                         authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        //                         SecurityContextHolder.getContext().setAuthentication(authToken);
        //                     }
        //                 }
        //             }
        //         }
        //     }
        // }


    private void authenticateUser(String token, boolean isAccessToken, HttpServletRequest request) {
    String username = jwtUtils.extractUsername(token, isAccessToken);
    log.debug("JWT Filter: Decoded username = {}", username);

    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = usersDetailsService.loadUserByUsername(username);
        log.debug("JWT Filter: Loaded userDetails for {}", username);

        boolean isValid = isAccessToken 
            ? jwtUtils.validateAccessToken(token) 
            : jwtUtils.validateRefreshToken(token);

        if (isValid) {
            log.debug("JWT Filter: Token is valid, setting authentication");
            UsernamePasswordAuthenticationToken authenticationToken = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
    }
}
}
