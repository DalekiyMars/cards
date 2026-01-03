package com.banking.cards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CardTransferRequest(
        @NotNull(message = "Source card id is required")
        Long fromCardId,

        @NotNull(message = "Target card id is required")
        Long toCardId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount
) {}
