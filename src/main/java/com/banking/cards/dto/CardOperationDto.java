package com.banking.cards.dto;

import com.banking.cards.common.CardOperationType;

import java.math.BigDecimal;
import java.time.Instant;

public record CardOperationDto(
        CardOperationType type,
        BigDecimal amount,
        String fromCard,
        String toCard,
        Instant createdAt
) {}