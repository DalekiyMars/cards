package com.banking.cards.dto;

import com.banking.cards.common.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardDto(
        Long id,
        String maskedNumber,
        LocalDate validityPeriod,
        CardStatus status,
        BigDecimal balance
) {}