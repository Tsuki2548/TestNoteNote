package com.project.notenote.user;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.notenote.user.dto.LoginRequest;
import com.project.notenote.user.dto.UsersRequest;
import com.project.notenote.user.dto.UsersResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api")
public class UsersController {
    private static final Logger log = LoggerFactory.getLogger(UsersController.class);

    @Autowired 
    private UsersService usersService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UsersRequest request) {
        log.debug("Server - register :{}", request.getUsername());
        try {
            UsersResponse usersResponse = usersService.addUser(request);
            return ResponseEntity.ok(usersResponse);
        } catch (com.project.notenote.user.exception.DuplicateUserException ex){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("error", ex.getMessage()));
        } catch (Exception ex){
            return ResponseEntity.internalServerError().body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> tokens = usersService.authenticate(request.usernameOrEmail(), request.password());

        if (tokens == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid username of password"));
        }

        String accessToken = tokens.get("ACCESS_TOKEN");
        String refreshToken = tokens.get("REFRESH_TOKEN");
        log.debug("Login successful for user, tokens generated");
        
        ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", accessToken)
            .httpOnly(true)
            .secure(false)
            .path("/")
            .sameSite("Strict")
            .maxAge(Duration.ofDays(7))
            .build();

        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", refreshToken)
            .httpOnly(true)
            .secure(false)
            .path("/")
            .sameSite("Strict")
            .maxAge(Duration.ofDays(7))
            .build();
        
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok("Login successful");
    }

    @GetMapping("/validate-token")
    public ResponseEntity<String> validation(@CookieValue(name = "ACCESS_TOKEN", required = false) String token) {
        log.debug("Token validation request received");

        if (token == null || !usersService.validateAccessToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token");
        }
        
        return ResponseEntity.ok("Token valid");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<String> refreshToken(@CookieValue(name = "REFRESH_TOKEN") String refreshToken) {
        log.debug("Token refresh request received");

        try {
            String newAccessToken = usersService.refreshAccessToken(refreshToken);
            log.info("New Access token: " +newAccessToken);
            ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", newAccessToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofMinutes(15))
                .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                    .body("Access token refreshed");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Boolean> logout(HttpServletResponse response) {
        ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", null)
            .httpOnly(true)
            .secure(false)
            .path("/")
            .sameSite("Strict")
            .maxAge(0)
            .build();
        
        ResponseCookie refreshToken = ResponseCookie.from("REFRESH_TOKEN", null)
            .httpOnly(true)
            .secure(false)
            .path("/")
            .sameSite("Strict")
            .maxAge(0)
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshToken.toString());
        log.info("logged out");
        return ResponseEntity.ok(true);
    }

    @GetMapping("/username")
    public ResponseEntity<String> getUsernameByToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        log.debug("Received token for username extraction");
        String username = usersService.getUsernameByToken(token);
        log.debug("Username decoded successfully");
        return ResponseEntity.ok(username);
    }

    // New: fetch public user info by username (e.g., username and email)
    @GetMapping("/users/{username}")
    public ResponseEntity<com.project.notenote.user.dto.UsersResponse> getUserInfoByUsername(@PathVariable String username) {
        var userInfo = usersService.getUserInfoByUsername(username);
        return ResponseEntity.ok(userInfo);
    }
}
