package com.banking.cards.mapper;

import com.banking.cards.dto.CardResponseDto;
import com.banking.cards.entity.Card;

public final class CardMapper {

    public static CardResponseDto toDto(Card card) {
        return new CardResponseDto(
                card.getId(),
                card.getMaskedNumber(),
                card.getValidityPeriod(),
                card.getStatus(),
                card.getBalance()
        );
    }
}
