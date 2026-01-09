package com.banking.cards.dto.response;

import com.banking.cards.common.CardStatus;

import java.time.LocalDate;

public record AdminCardDto(
        String maskedNumber,
        LocalDate validityPeriod,
        CardStatus status,
        String balance
) {}
