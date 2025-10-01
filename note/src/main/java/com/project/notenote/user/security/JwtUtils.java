package com.project.notenote.user.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;



@Component
public class JwtUtils {
    @Value("${jwt.access-secret}")
    private String accessSecret;

    @Value("${jwt.refresh-secret}")
    private String refreshSecret;

    private final long ACCESS_TOKEN_VALDITY_MS = 5 * 1000; // 15 minute
    private final long REFRESH_TOKEN_VALIDITY_MS = 7 * 24 * 60 * 60 *1000; // 7 days

    private Key getAccessSigningKey() {
        return Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
    }

    private Key getRefreshSigningKey() {
        return Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token, Boolean isAccess) {
        Claims claims =  Jwts.parserBuilder()
                .setSigningKey(isAccess ? getAccessSigningKey() : getRefreshSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    private boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }

    public String generateAccessToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALDITY_MS))
                .signWith(getAccessSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY_MS))
                .signWith(getRefreshSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateAccessToken(String token) {
        return validateTokenWithKey(token, getAccessSigningKey(), "access");
    }

    public boolean validateRefreshToken(String token) {
        return validateTokenWithKey(token, getRefreshSigningKey(), "refresh");
    }

    private boolean validateTokenWithKey(String token, Key key, String expectedType) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String type = claims.get("type", String.class);
            return expectedType.equals(type) && !isTokenExpired(claims.getExpiration());
        } catch (Exception e) {
            return false;
        }
    }

    public String refreshAccessToken(String refreshToken) {
    // ตรวจสอบว่า refresh token ถูกต้อง
        if (!validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        // ดึง username จาก refresh token
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getRefreshSigningKey())
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        String username = claims.getSubject();

        // ออก access token ใหม่
        return generateAccessToken(username);
    }

}
