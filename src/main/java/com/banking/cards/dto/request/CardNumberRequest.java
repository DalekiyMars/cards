package com.banking.cards.dto.request;

import com.banking.cards.common.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Setter;

@Schema(description = "Запрос с номером карты")
@Setter
public class CardNumberRequest {
    @Schema(
            description = "Номер банковской карты",
            example = "4276550012345678"
    )
    @NotBlank(message = "Card number required")
    @Pattern(regexp = Constants.CARD_PATTERN, message = "Wrong card number")
    String cardNumber;

    public String getCardNumber() {
        return cardNumber.trim().replaceAll("[^0-9]", "");
    }
}
