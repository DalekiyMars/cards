package com.banking.cards.controller;

import com.banking.cards.dto.request.LoginRequest;
import com.banking.cards.dto.request.SideServiceRequest;
import com.banking.cards.dto.response.JwtResponse;
import com.banking.cards.security.JwtService;
import com.banking.cards.security.SecurityUser;
import com.banking.cards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtUtil;
    private final UserService userService;

    // ===== REGISTRATION =====

    @Operation(
            summary = "Регистрация пользователя",
            description = "Создаёт нового пользователя с ролью USER"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные или пользователь уже существует")
    })
    @PostMapping("/register")
    public HttpStatus register(
            @Valid @RequestBody LoginRequest request
    ) {
        userService.register(request);
        return HttpStatus.CREATED;
    }

    // ===== LOGIN =====

    @PostMapping("/login")
    public JwtResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        SecurityUser user = (SecurityUser) authentication.getPrincipal();
        String token = jwtUtil.generateToken(user);

        return new JwtResponse(token);
    }

    @Operation(summary = "Получить токен для внешнего сервиса")
    @GetMapping("/token")
    public JwtResponse getIntegrationToken(@Valid @RequestBody SideServiceRequest request) {
        return new JwtResponse(jwtUtil.generateIntegrationToken(request));
    }
}
