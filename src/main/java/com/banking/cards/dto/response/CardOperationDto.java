package com.banking.cards.dto.response;

import com.banking.cards.common.CardOperationType;
import com.banking.cards.common.MaskedCardNumber;

import java.math.BigDecimal;
import java.time.Instant;

public record CardOperationDto(
        CardOperationType type,
        BigDecimal amount,
        MaskedCardNumber fromCard,
        MaskedCardNumber toCard,
        Instant createdAt
) {}