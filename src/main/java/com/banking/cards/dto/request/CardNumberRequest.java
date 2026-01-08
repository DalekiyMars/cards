package com.banking.cards.dto.request;

import com.banking.cards.common.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Setter;

@Setter
public class CardNumberRequest {
    @NotBlank
    @Pattern(regexp = Constants.CARD_PATTERN, message = "Wrong card number")
    String cardNumber;

    public String getCardNumber() {
        return cardNumber.trim().replaceAll("[^0-9]", "");
    }
}
