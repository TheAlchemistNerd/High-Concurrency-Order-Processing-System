package com.ecommerce.orderprocessing.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Modern JWT Token Provider for handling JWT operations using java.time.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final Duration expiration;
    private final long expirationTimeMillis;

    public JwtTokenProvider(@Value("${spring.security.jwt.secret-key}") String secretKeyValue,
                            @Value("${spring.security.jwt.expiration}") long expirationTimeMillis) {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyValue.getBytes());
        this.expiration = Duration.ofMillis(expirationTimeMillis);
        this.expirationTimeMillis = expirationTimeMillis;
    }

    public String generateToken(String email, String role) {
        var now = Instant.now();
        var expiryDate = now.plus(this.expiration);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(secretKey)
                .compact();
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsernameFromToken(String token) {
        return parseToken(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return parseToken(token).get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Long getExpirationTime() {
        return this.expirationTimeMillis;
    }
}
