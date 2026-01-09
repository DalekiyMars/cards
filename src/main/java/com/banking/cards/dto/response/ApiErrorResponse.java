package com.banking.cards.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(
        name = "ApiErrorResponse",
        description = "Стандартный ответ об ошибке"
)
public record ApiErrorResponse(

        @Schema(
                description = "Время возникновения ошибки",
                example = "2026-01-08T12:30:15.123Z"
        )
        Instant timestamp,

        @Schema(
                description = "HTTP статус код",
                example = "404"
        )
        int status,

        @Schema(
                description = "HTTP статус",
                example = "Not Found"
        )
        String error,

        @Schema(
                description = "Сообщение об ошибке",
                example = "User not found"
        )
        String message,

        @Schema(
                description = "URI запроса",
                example = "/api/users/cards"
        )
        String path
) {}
