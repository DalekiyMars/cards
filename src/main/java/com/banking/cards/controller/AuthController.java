package com.banking.cards.controller;

import com.banking.cards.dto.request.LoginRequest;
import com.banking.cards.dto.request.SideServiceRequest;
import com.banking.cards.dto.response.ApiErrorResponse;
import com.banking.cards.dto.response.JwtResponse;
import com.banking.cards.security.JwtService;
import com.banking.cards.security.SecurityUser;
import com.banking.cards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description = "Регистрация пользователей и получение JWT токенов"
)
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtUtil;
    private final UserService userService;

    // ===== REGISTRATION =====

    @Operation(
            summary = "Регистрация пользователя",
            description = """
                    Создаёт нового пользователя с ролью USER.
                    
                    После успешной регистрации пользователь может выполнить вход
                    и получить JWT токен.
                    """
    )
    @ApiResponses({

            // ===== 201 =====
            @ApiResponse(
                    responseCode = "201",
                    description = "Пользователь успешно зарегистрирован"
            ),

            // ===== 400 =====
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные или пользователь уже существует",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-01-08T12:00:00.000Z",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "User already exists",
                                      "path": "/api/auth/register"
                                    }
                                    """)
                    )
            )
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(
            @Valid
             @io.swagger.v3.oas.annotations.parameters.RequestBody(
                     description = "Данные для регистрации пользователя",
                     required = true,
                     content = @Content(
                             mediaType = "application/json",
                             examples = @ExampleObject(value = """
                                                        {
                                                          "username": "john.doe",
                                                          "password": "StrongPassword123!"
                                                        }
                                                        """)
                                        )
            )
             @RequestBody LoginRequest request) {
        userService.register(request);
    }

    // ===== LOGIN =====

    @Operation(
            summary = "Вход в систему",
            description = """
                    Аутентифицирует пользователя по логину и паролю.
                    
                    В случае успеха возвращает JWT токен, который необходимо
                    передавать в заголовке Authorization:
                    
                    `Authorization: Bearer <token>`
                    """
    )
    @ApiResponses({

            // ===== 200 =====
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная аутентификация",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JwtResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                                    }
                                    """)
                    )
            ),

            // ===== 401 / 403 =====
            @ApiResponse(
                    responseCode = "403",
                    description = "Неверный логин или пароль",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-01-08T12:10:00.000Z",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Bad credentials",
                                      "path": "/api/auth/login"
                                    }
                                    """)
                    )
            )
    })
    @PostMapping("/login")
    public JwtResponse login(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для входа",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(value = """
                                    {
                                      "username": "john.doe",
                                      "password": "StrongPassword123!"
                                    }
                                    """)
                    )
            )
            @RequestBody LoginRequest request
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

    // ===== INTEGRATION TOKEN =====

    @Operation(
            summary = "Получить интеграционный JWT токен (S2S)",
            description = """
                    Используется для server-to-server интеграций.
                    
                    Обменивает имя сервиса и API Key на JWT токен.
                    """
    )
    @ApiResponses({

            // ===== 200 =====
            @ApiResponse(
                    responseCode = "200",
                    description = "Интеграционный токен успешно создан",
                    content = @Content(
                            schema = @Schema(implementation = JwtResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                                    }
                                    """)
                    )
            ),

            // ===== 401 / 403 =====
            @ApiResponse(
                    responseCode = "403",
                    description = "Неверное имя сервиса или API Key",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-01-08T12:15:00.000Z",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Invalid service credentials",
                                      "path": "/api/auth/token"
                                    }
                                    """)
                    )
            )
    })
    @PostMapping("/token")
    public JwtResponse getIntegrationToken(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные стороннего сервиса",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(value = """
                                    {
                                      "serviceName": "billing-service",
                                      "apiKey": "b1f8c9d2-4a7e-4f12-a89d-123456789abc"
                                    }
                                    """)
                    )
            )
            @RequestBody SideServiceRequest request
    ) {
        return new JwtResponse(jwtUtil.generateIntegrationToken(request));
    }
}