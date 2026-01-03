package com.banking.cards.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CardTransferRequest(
        @NotNull(message = "Source card id is required")
        @Min(1)
        UUID fromCardId,

        @NotNull(message = "Target card id is required")
        @Min(1)
        UUID toCardId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount
) {}
