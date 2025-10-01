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
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UsersDetailsService usersDetailsService;

    @Override
protected void doFilterInternal(
    HttpServletRequest request, 
    HttpServletResponse response, 
    FilterChain filterChain)
        throws ServletException, IOException {

    String accessToken = null;
    String refreshToken = null;

    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        accessToken = authHeader.substring(7);
        System.out.println("JWT Filter: Found accessToken in header");
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
        System.out.println("JWT Filter: accessToken valid, authenticating user...");
        authenticateUser(accessToken, true, request);
    } else if (refreshToken != null && jwtUtils.validateRefreshToken(refreshToken)) {
        System.out.println("JWT Filter: refreshing accessToken...");
        String newAccessToken = jwtUtils.refreshAccessToken(refreshToken);

        Cookie newAccessCookie = new Cookie("ACCESS_TOKEN", newAccessToken);
        newAccessCookie.setHttpOnly(true);
        newAccessCookie.setSecure(false);
        newAccessCookie.setPath("/");
        response.addCookie(newAccessCookie);

        authenticateUser(newAccessToken, true, request);
    } else {
        System.out.println("JWT Filter: No valid token found");
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
    System.out.println("JWT Filter: Decoded username = " + username);

    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = usersDetailsService.loadUserByUsername(username);
        System.out.println("JWT Filter: Loaded userDetails for " + username);

        boolean isValid = isAccessToken 
            ? jwtUtils.validateAccessToken(token) 
            : jwtUtils.validateRefreshToken(token);

        if (isValid) {
            System.out.println("JWT Filter: Token is valid, setting authentication");
            UsernamePasswordAuthenticationToken authenticationToken = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
    }
}
}
