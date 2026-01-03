package com.banking.cards.dto;

import com.banking.cards.common.CardStatus;
import jakarta.persistence.Enumerated;

public record CardStatusRequest(
        @Enumerated
        CardStatus status
) {}
