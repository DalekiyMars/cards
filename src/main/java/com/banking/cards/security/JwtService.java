package com.banking.cards.security;

import com.banking.cards.dto.request.SideServiceRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationMs;
    @Getter
    private final String sideServiceName;

    public JwtService(
            @Value("${application.security.jwt.secret-key}") String secret,
            @Value("${application.security.jwt.expiration}") long expirationMs,
            @Value("${application.security.jwt.service-name}") String sideServiceName

    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.sideServiceName = sideServiceName;
    }

    /**
     * JWT payload:
     *  - sub    -> userId
     *  - roles  -> ["ROLE_USER"]
     */
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
        if (!request.service().equalsIgnoreCase(sideServiceName)) {
            throw new IllegalArgumentException("Invalid service name");
        }
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(sideServiceName)
                .setIssuedAt(now)
                .setExpiration(exp)
                .claim("roles", List.of("ROLE_INTEGRATION")) // Специальная роль
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String extractUserId(String token) {
        return getClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return getClaims(token).get("roles", List.class);
    }
}
