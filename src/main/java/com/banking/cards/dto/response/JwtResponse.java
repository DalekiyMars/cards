package com.banking.cards.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "JWT токен для аутентификации"
)
public record JwtResponse(
        @Schema(
                description = "JWT токен (Bearer)",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        String token
) {}