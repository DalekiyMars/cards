package com.banking.cards.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

@Schema(description = "Запрос на создание банковской карты")
public record AdminCreateCardRequest(

        @NotNull
        UUID userId,

        @Schema(
                description = "Срок действия карты",
                example = "2029-12"
        )
        @NotNull
        @Future
        YearMonth validityPeriod,

        @Schema(
                description = "Начальный баланс карты",
                example = "0.00"
        )
        @NotNull
        @DecimalMin(value = "0", message = "Amount must be greater than zero")
        @Digits(integer = 19, fraction = 2)
        BigDecimal initialBalance
) {}