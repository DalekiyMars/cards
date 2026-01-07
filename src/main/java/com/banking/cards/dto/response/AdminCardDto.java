package com.banking.cards.dto.response;

import com.banking.cards.common.CardStatus;

import java.time.LocalDate;
import java.util.UUID;

public record AdminCardDto(
        UUID id,
        String maskedNumber,
        LocalDate validityPeriod,
        CardStatus status,
        String balance
) {}
