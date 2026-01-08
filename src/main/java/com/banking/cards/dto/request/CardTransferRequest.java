package com.banking.cards.dto.request;

import com.banking.cards.common.Constants;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record CardTransferRequest(
        @NotBlank(message = "Source card id is required")
        @Pattern(regexp = Constants.CARD_PATTERN, message = "Card invalid")
        String fromCardId,

        @NotBlank(message = "Target card id is required")
        @Pattern(regexp = Constants.CARD_PATTERN, message = "Card invalid")
        String toCardId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        @Digits(integer = 19, fraction = 2)
        BigDecimal amount
) {}
