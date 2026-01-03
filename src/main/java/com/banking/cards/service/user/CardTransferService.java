package com.banking.cards.service.user;

import com.banking.cards.dto.CardTransferRequest;
import com.banking.cards.common.CardStatus;
import com.banking.cards.entity.Card;
import com.banking.cards.repository.CardRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CardTransferService {

    private final CardRepository cardRepository;

    @Transactional
    public void transfer(Long userId, CardTransferRequest request) {

        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        Card from = cardRepository.findByIdForUpdate(request.fromCardId())
                .orElseThrow(() -> new EntityNotFoundException("Source card not found"));

        Card to = cardRepository.findByIdForUpdate(request.toCardId())
                .orElseThrow(() -> new EntityNotFoundException("Target card not found"));

        if (!from.getOwner().getId().equals(userId)
                || !to.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("You can transfer only between your cards");
        }

        // Проверка статусов
        validateCardForTransfer(from);
        validateCardForTransfer(to);

        // Проверка баланса
        if (from.getBalance().compareTo(request.amount()) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        from.setBalance(from.getBalance().subtract(request.amount()));
        to.setBalance(to.getBalance().add(request.amount()));
    }

    private void validateCardForTransfer(Card card) {
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Card is not active");
        }
        if (card.getValidityPeriod().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Card is expired");
        }
    }
}
