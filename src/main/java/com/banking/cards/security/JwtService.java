package com.banking.cards.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtService(@Value("${application.security.jwt.secret-key}") String secret,
                      @Value("${application.security.jwt.expiration}") long expirationMs) {
        if (Objects.isNull(secret) || secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be provided and at least 32 characters long");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Генерирует JWT. В payload кладём:
     *  - sub = userId (String)
     *  - username = user.getUsername()
     *  - roles = List из roles (например ["ROLE_USER"])
     */
    public String generateToken(Long userId, String username, List<String> roles) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        JwtBuilder builder = Jwts.builder()
                .setSubject(userId.toString())                // sub -> userId
                .setIssuedAt(now)
                .setExpiration(exp)
                .claim("username", username)                  // дополнительный claim
                .claim("roles", roles)                        // роли
                .signWith(secretKey, SignatureAlgorithm.HS256);

        return builder.compact();
    }

    /** Разбор claims и валидация подписи/срока */
    public Jws<Claims> parseClaimsJws(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
    }

    /** Проверка валидности токена (сигнатура + срок) */
    public boolean validateToken(String token) {
        try {
            parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    /** Получить все claims */
    public Claims getClaims(String token) {
        return parseClaimsJws(token).getBody();
    }

    /** Вытянуть userId */
    public Long extractUserId(String token) {
        String sub = getClaims(token).getSubject();
        return sub != null ? Long.valueOf(sub) : null;
    }

    /** Вытянуть username */
    public String extractUsername(String token) {
        return getClaims(token).get("username", String.class);
    }

    /** Вытянуть роли в виде List<String> */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object raw = getClaims(token).get("roles");
        if (raw instanceof List) {
            return (List<String>) raw;
        }
        return List.of();
    }
}