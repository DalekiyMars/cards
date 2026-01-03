package com.banking.cards.mapper;

import com.banking.cards.dto.CardDto;
import com.banking.cards.dto.CardOperationDto;
import com.banking.cards.entity.Card;
import com.banking.cards.entity.CardOperation;

public final class CardMapper {

    public static CardDto toDto(Card card) {
        return new CardDto(
                card.getId(),
                card.getMaskedNumber(),
                card.getValidityPeriod(),
                card.getStatus(),
                card.getBalance()
        );
    }

    public static CardOperationDto toOperationDto(CardOperation op) {
        return new CardOperationDto(
                op.getType(),
                op.getAmount(),
                op.getFromCard() != null ? op.getFromCard().getMaskedNumber() : null,
                op.getToCard() != null ? op.getToCard().getMaskedNumber() : null,
                op.getCreatedAt()
        );
    }
}
