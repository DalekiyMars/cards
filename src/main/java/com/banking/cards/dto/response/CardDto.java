package com.banking.cards.dto.response;

import com.banking.cards.common.CardStatus;
import com.banking.cards.common.MaskedBalanceValue;
import com.banking.cards.common.MaskedCardNumber;

import java.time.LocalDate;
import java.util.UUID;

public record CardDto (
        UUID id,
        MaskedCardNumber maskedNumber,
        LocalDate validityPeriod,
        CardStatus status,
        MaskedBalanceValue balance)
{}