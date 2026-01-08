package com.banking.cards.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // Если Optional не пустой, значит токен валиден.
                jwtService.parseToken(token).ifPresent(claims -> {

                    String userId = claims.getSubject();
                    @SuppressWarnings("unchecked")
                    List<String> roles = claims.get("roles", List.class);

                    List<SimpleGrantedAuthority> authorities = (roles == null)
                            ? List.of() // Защита от NPE если ролей нет
                            : roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList();

                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userId, // Principal
                            null,   // Credentials
                            authorities
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
            }
        }
        filterChain.doFilter(request, response);
    }
}
