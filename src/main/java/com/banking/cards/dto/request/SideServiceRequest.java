package com.banking.cards.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(
        description = "Запрос для получения интеграционного (S2S) токена"
)
public record SideServiceRequest(
        @Schema(
                description = "Имя стороннего сервиса",
                example = "billing-service"
        )
        @NotBlank String service,

        @Schema(
                description = "API ключ сервиса",
                example = "b1f8c9d2-4a7e-4f12-a89d-123456789abc"
        )
        @NotBlank String apiKey
) {}