package com.banking.cards.dto.response;

import com.banking.cards.common.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CardDto(
        UUID id,
        String maskedNumber,
        LocalDate validityPeriod,
        CardStatus status,
        BigDecimal balance
) {}