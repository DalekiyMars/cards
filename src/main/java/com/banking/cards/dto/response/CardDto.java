package com.banking.cards.dto.response;

import com.banking.cards.common.CardStatus;
import com.banking.cards.common.MaskedCardNumber;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CardDto {
    private UUID id;
    private MaskedCardNumber maskedNumber;
    private LocalDate validityPeriod;
    private CardStatus status;
    private BigDecimal balance;

    public CardDto(
                UUID id,
                MaskedCardNumber maskedNumber,
                LocalDate validityPeriod,
                CardStatus status,
                BigDecimal balance
    ) {
        this.id = id;
        this.maskedNumber = maskedNumber;
        this.validityPeriod = validityPeriod;
        this.status = status;
        this.balance = balance;
    }
}