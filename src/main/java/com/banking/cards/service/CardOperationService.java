package com.banking.cards.service;

import com.banking.cards.common.CardOperationType;
import com.banking.cards.entity.Card;
import com.banking.cards.entity.CardOperation;
import com.banking.cards.repository.CardOperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CardOperationService {

    private final CardOperationRepository repository;

    public void logDeposit(Card card, BigDecimal amount) {
        save(null, card, CardOperationType.DEPOSIT, amount);
    }

    public void logWithdraw(Card card, BigDecimal amount) {
        save(card, null, CardOperationType.WITHDRAW, amount);
    }

    public void logTransfer(Card from, Card to, BigDecimal amount) {
        save(from, to, CardOperationType.TRANSFER, amount);
    }

    private void save(
            Card from,
            Card to,
            CardOperationType type,
            BigDecimal amount
    ) {
        repository.save(
                CardOperation.builder()
                        .fromCard(from)
                        .toCard(to)
                        .type(type)
                        .amount(amount)
                        .createdAt(Instant.now())
                        .build()
        );
    }
}