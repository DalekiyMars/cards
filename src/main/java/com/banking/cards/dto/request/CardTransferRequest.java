package com.banking.cards.dto.request;

import com.banking.cards.common.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

@Schema(description = "Запрос на перевод средств между картами")
public record CardTransferRequest(
        @Schema(
                description = "Номер карты отправителя",
                example = "4276550012345678"
        )
        @NotBlank(message = "Source card id is required")
        @Pattern(regexp = Constants.CARD_PATTERN, message = "Card invalid")
        String fromCardId,


        @Schema(
                description = "Номер карты получателя",
                example = "4276550098765432"
        )
        @NotBlank(message = "Target card id is required")
        @Pattern(regexp = Constants.CARD_PATTERN, message = "Card invalid")
        String toCardId,

        @Schema(
                description = "Сумма перевода",
                example = "500.00"
        )
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        @Digits(integer = 19, fraction = 2)
        BigDecimal amount
) {}
