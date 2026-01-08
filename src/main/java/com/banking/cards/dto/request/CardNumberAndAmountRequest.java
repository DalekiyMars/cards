package com.banking.cards.dto.request;

import com.banking.cards.common.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

@Schema(description = "Запрос операции по карте")
public record CardNumberAndAmountRequest(

        @NotBlank(message = "Card number required")
        @Pattern(regexp = Constants.CARD_PATTERN, message = "Wrong card number")
        String cardNumber,

        @Schema(
                description = "Сумма операции",
                example = "500.00"
        )
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        @Digits(integer = 19, fraction = 2)
        BigDecimal amount
) {}
