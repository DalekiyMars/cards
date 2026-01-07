package com.banking.cards.mapper;

import com.banking.cards.common.MaskedBalanceValue;
import com.banking.cards.common.MaskedCardNumber;
import com.banking.cards.common.MaskedValueFactory;
import com.banking.cards.dto.response.AdminCardDto;
import com.banking.cards.dto.response.CardDto;
import com.banking.cards.dto.response.CardOperationDto;
import com.banking.cards.entity.Card;
import com.banking.cards.entity.CardOperation;
import com.banking.cards.util.MathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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
                toMaskedCardBalance(MathUtil.roundBalanceTo2SympolsAfterPoint(card.getBalance()))
        );
    }

    public AdminCardDto toAdminDto(Card card) {
        return new AdminCardDto(
                card.getUniqueKey(),
                toMaskedCardNumber(card.getCardNumber()).toString(),
                card.getValidityPeriod(),
                card.getStatus(),
                toMaskedCardBalance(card.getBalance()).toString()
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

    public MaskedBalanceValue toMaskedCardBalance(BigDecimal cardBalance) {
        return maskedValueFactory.createCardNumber(cardBalance);
    }

    public BigDecimal fromMaskedCardBalance(MaskedBalanceValue cardNumber) {
        return cardNumber.value();
    }
}
