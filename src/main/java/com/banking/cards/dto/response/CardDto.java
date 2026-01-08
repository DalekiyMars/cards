package com.banking.cards.dto.response;

import com.banking.cards.common.CardStatus;
import com.banking.cards.common.MaskedBalanceValue;
import com.banking.cards.common.MaskedCardNumber;

import java.time.LocalDate;

public record CardDto (
        MaskedCardNumber maskedNumber,
        LocalDate validityPeriod,
        CardStatus status,
        MaskedBalanceValue balance)
{}