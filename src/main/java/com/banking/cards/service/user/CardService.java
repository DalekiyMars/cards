package com.banking.cards.service.user;

import com.banking.cards.common.CardStatus;
import com.banking.cards.entity.Card;
import com.banking.cards.entity.User;
import com.banking.cards.repository.CardRepository;
import com.banking.cards.service.CardOperationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final CardOperationService cardOperationService;

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount, User user) {
        Card from = getUserCard(fromId, user);
        Card to = getUserCard(toId, user);

        validateCardIsActive(from);
        validateCardIsActive(to);

        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        cardOperationService.logTransfer(from, to, amount);
    }

    @Transactional
    public void deposit(Long cardId, BigDecimal amount, User user) {

        Card card = getUserCard(cardId, user);
        validateCardIsActive(card);

        card.setBalance(card.getBalance().add(amount));
        cardOperationService.logDeposit(card, amount);
    }

    @Transactional
    public void withdraw(Long cardId, BigDecimal amount, User user) {
        Card card = getUserCard(cardId, user);
        validateCardIsActive(card);

        if (card.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        card.setBalance(card.getBalance().subtract(amount));
        cardOperationService.logWithdraw(card, amount);
    }

    // ===== PRIVATE HELPERS =====

    private Card getUserCard(Long cardId, User user) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));

        if (!card.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Card does not belong to user");
        }
        return card;
    }

    private void validateCardIsActive(Card card) {
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Card is not active");
        }
    }
}
