package com.banking.cards.service.user;

import com.banking.cards.common.CardStatus;
import com.banking.cards.entity.Card;
import com.banking.cards.entity.User;
import com.banking.cards.repository.CardRepository;
import com.banking.cards.repository.UserRepository;
import com.banking.cards.service.CardOperationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserCardOperationService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardOperationService cardOperationService;

    @Transactional
    public void transfer(UUID fromId, UUID toId, BigDecimal amount, Long userId) {
        User user = getUser(userId);

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
    public void deposit(UUID cardId, BigDecimal amount, Long userId) {
        User user = getUser(userId);

        Card card = getUserCard(cardId, user);
        validateCardIsActive(card);

        card.setBalance(card.getBalance().add(amount));
        cardOperationService.logDeposit(card, amount);
    }

    @Transactional
    public void withdraw(UUID cardId, BigDecimal amount, Long userId) {
        User user = getUser(userId);

        Card card = getUserCard(cardId, user);
        validateCardIsActive(card);

        if (card.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        card.setBalance(card.getBalance().subtract(amount));
        cardOperationService.logWithdraw(card, amount);
    }

    // ===== HELPERS =====

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private Card getUserCard(UUID cardId, User user) {
        return cardRepository.findByUniqueKeyAndOwner(cardId, user)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));
    }

    private void validateCardIsActive(Card card) {
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Card is not active");
        }
    }
}
