package com.banking.cards.service.user;

import com.banking.cards.common.CardStatus;
import com.banking.cards.common.audit.AuditAction;
import com.banking.cards.common.audit.AuditEntityType;
import com.banking.cards.entity.Card;
import com.banking.cards.entity.User;
import com.banking.cards.repository.CardRepository;
import com.banking.cards.repository.UserRepository;
import com.banking.cards.service.AuditService;
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
    private final AuditService auditService;

    @Transactional
    public void transfer(String fromId, String toId, BigDecimal amount, UUID userId) {
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

        auditService.log(
                AuditAction.CARD_TRANSFER_OUT,
                AuditEntityType.CARD,
                from.getCardNumber(),
                "to=" + to.getCardNumber() + ";amount=" + amount
        );

        auditService.log(
                AuditAction.CARD_TRANSFER_IN,
                AuditEntityType.CARD,
                to.getCardNumber(),
                "from=" + from.getCardNumber() + ";amount=" + amount
        );


    }

    @Transactional
    public void deposit(String cardId, BigDecimal amount, UUID userId) {
        User user = getUser(userId);

        Card card = getUserCard(cardId, user);
        validateCardIsActive(card);

        card.setBalance(card.getBalance().add(amount));
        cardOperationService.logDeposit(card, amount);

        auditService.log(
                AuditAction.CARD_DEPOSIT,
                AuditEntityType.CARD,
                card.getCardNumber(),
                "amount=" + amount
        );
    }

    @Transactional
    public void withdraw(String cardId, BigDecimal amount, UUID userId) {
        User user = getUser(userId);

        Card card = getUserCard(cardId, user);
        validateCardIsActive(card);

        if (card.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        var before = card.getBalance();
        card.setBalance(card.getBalance().subtract(amount));
        cardOperationService.logWithdraw(card, amount);

        auditService.log(
                AuditAction.CARD_WITHDRAW,
                AuditEntityType.CARD,
                card.getCardNumber(),
                "amount=" + amount + ";balanceBefore=" + before
        );

    }

    // ===== HELPERS =====

    private User getUser(UUID userId) {
        return userRepository.findByUniqueKey(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private Card getUserCard(String cardId, User user) {
        return cardRepository.findByCardNumberAndOwner(cardId, user)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));
    }

    private void validateCardIsActive(Card card) {
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Card is not active");
        }
    }
}
