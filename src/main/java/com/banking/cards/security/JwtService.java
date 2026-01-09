package com.banking.cards.security;

import com.banking.cards.dto.request.SideServiceRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private final SecretKey secretKey;
    private final long expirationMs;
    @Getter
    private final String sideServiceName;
    private final String integrationApiKey;

    public JwtService(
            @Value("${application.security.jwt.secret-key}") String secret,
            @Value("${application.security.jwt.expiration}") long expirationMs,
            @Value("${application.security.integration.service-name}") String sideServiceName,
            @Value("${application.security.integration.api-key}") String integrationApiKey
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.sideServiceName = sideServiceName;
        this.integrationApiKey = integrationApiKey;
    }

    public String generateToken(SecurityUser user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .setSubject(user.getUniqueKey().toString())
                .setIssuedAt(now)
                .setExpiration(exp)
                .claim("roles", roles)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateIntegrationToken(SideServiceRequest request) {
        if (!sideServiceName.equalsIgnoreCase(request.service())) {
            throw new BadCredentialsException("Invalid service name");
        }

        if (!integrationApiKey.equals(request.apiKey())) {
            throw new BadCredentialsException("Invalid API Key");
        }

        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(sideServiceName)
                .setIssuedAt(now)
                .setExpiration(exp)
                .claim("roles", List.of("ROLE_INTEGRATION"))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // --- ОПТИМИЗАЦИЯ ЗДЕСЬ ---

    /**
     * Пытается распарсить токен.
     * Возвращает Optional с Claims, если токен валиден.
     * Возвращает empty, если токен истек или подпись неверна.
     */
    public Optional<Claims> parseToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT: {}",e.getMessage());
            return Optional.empty();
        }
    }
}