package com.banking.cards.dto.response;

import com.banking.cards.common.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.YearMonth;

@Schema(description = "Представление банковской карты для администратора")
public record AdminCardDto(
        String maskedNumber,
        @Schema(example = "2029-12")
        YearMonth validityPeriod,
        @Schema(example = "ACTIVE")
        CardStatus status,
        @Schema(example = "0.00")
        String balance
) {}
