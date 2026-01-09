package com.banking.cards.dto.response;

import com.banking.cards.common.CardOperationType;
import com.banking.cards.common.MaskedCardNumber;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

public record CardOperationDto(
        @Schema(example = "DEPOSIT")
        CardOperationType type,
        @Schema(example = "500.00")
        BigDecimal amount,
        MaskedCardNumber fromCard,
        MaskedCardNumber toCard,
        @Schema(example = "2026-01-08T14:30:00Z")
        Instant createdAt
) {}