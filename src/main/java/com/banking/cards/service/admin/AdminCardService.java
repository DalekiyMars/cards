package com.banking.cards.service.admin;

import com.banking.cards.common.CardStatus;
import com.banking.cards.dto.request.AdminCreateCardRequest;
import com.banking.cards.dto.response.CardDto;
import com.banking.cards.dto.response.PageResponse;
import com.banking.cards.entity.Card;
import com.banking.cards.entity.User;
import com.banking.cards.exceptions.BadBalanceException;
import com.banking.cards.mapper.CardMapper;
import com.banking.cards.mapper.PageMapper;
import com.banking.cards.repository.CardRepository;
import com.banking.cards.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminCardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @Transactional
    public CardDto createCard(AdminCreateCardRequest request) {

        User user = userRepository.findByUniqueKey(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Card card = Card.builder()
                .cardNumber(generateCardNumber())
                .owner(user)
                .validityPeriod(request.validityPeriod())
                .status(CardStatus.ACTIVE)
                .balance(request.initialBalance())
                .build();

        Card saved = cardRepository.save(card);
        return CardMapper.toDto(saved);
    }

    @Transactional
    public void deleteCard(UUID cardId) {
        Card card = cardRepository.findByUniqueKey(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));
        if (!card.getBalance().equals(BigDecimal.ZERO))
            throw new BadBalanceException("Card balance should be zero");
        cardRepository.delete(card);
    }

    @Transactional
    public void changeStatus(UUID cardId, CardStatus status) {
        Card card = cardRepository.findByUniqueKey(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));

        card.setStatus(status);
        // save НЕ нужен — managed entity
    }

    @Transactional(readOnly = true)
    public PageResponse<CardDto> getUserCards(UUID userId, Pageable pageable) {

        User user = userRepository.findByUniqueKey(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Page<Card> cards = cardRepository.findAllByOwner(user, pageable);

        return PageMapper.toPageResponse(cards.map(CardMapper::toDto));
    }

    // ===== PRIVATE =====

    private String generateCardNumber() {
        // 16 цифр, BIN можно захардкодить
        return "4000" + UUID.randomUUID().toString()
                .replaceAll("\\D", "")
                .substring(0, 12);
    }
}
