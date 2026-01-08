package com.banking.cards.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        description = "Запрос для регистрации или входа пользователя"
)
public record LoginRequest(
        @Schema(description = "Имя пользователя", example = "john_doe")
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50)
        String username,

        @Schema(description = "Пароль", example = "secret123")
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password
) {}