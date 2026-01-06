package com.banking.cards.mapper;

import com.banking.cards.common.MaskedCardNumber;
import com.banking.cards.common.MaskedValueFactory;
import com.banking.cards.dto.response.CardDto;
import com.banking.cards.dto.response.CardOperationDto;
import com.banking.cards.entity.Card;
import com.banking.cards.entity.CardOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {
    @Autowired
    private MaskedValueFactory maskedValueFactory;

    public CardDto toDto(Card card) {
        return new CardDto(
                card.getUniqueKey(),
                toMaskedCardNumber(card.getCardNumber()),
                card.getValidityPeriod(),
                card.getStatus(),
                card.getBalance()
        );
    }

    public CardOperationDto toOperationDto(CardOperation op) {
        return new CardOperationDto(
                op.getType(),
                op.getAmount(),
                op.getFromCard() != null ? toMaskedCardNumber(op.getFromCard().getCardNumber()) : null,
                op.getToCard() != null ? toMaskedCardNumber(op.getToCard().getCardNumber()) : null,
                op.getCreatedAt()
        );
    }

    public MaskedCardNumber toMaskedCardNumber(String cardNumber) {
        return maskedValueFactory.createCardNumber(cardNumber);
    }

    public String fromMaskedCardNumber(MaskedCardNumber cardNumber) {
        return cardNumber.value();
    }
}
