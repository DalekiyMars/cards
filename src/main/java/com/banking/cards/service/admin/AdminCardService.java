package com.banking.cards.service.admin;

import com.banking.cards.common.CardStatus;
import com.banking.cards.common.audit.AuditAction;
import com.banking.cards.common.audit.AuditEntityType;
import com.banking.cards.config.CardSettingsConfig;
import com.banking.cards.dto.request.AdminCreateCardRequest;
import com.banking.cards.dto.response.AdminCardDto;
import com.banking.cards.dto.response.PageResponse;
import com.banking.cards.entity.Card;
import com.banking.cards.entity.User;
import com.banking.cards.exceptions.BadBalanceException;
import com.banking.cards.mapper.CardMapper;
import com.banking.cards.mapper.PageMapper;
import com.banking.cards.repository.CardRepository;
import com.banking.cards.repository.UserRepository;
import com.banking.cards.service.AuditService;
import com.banking.cards.util.CardNumberGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminCardService {

    private static final Logger log = LoggerFactory.getLogger(AdminCardService.class);
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final CardMapper mapper;
    private final CardSettingsConfig cardConfig;
    private final CardNumberGenerator cardNumberGenerator;

    @Transactional
    public AdminCardDto createCard(AdminCreateCardRequest request) {
        Card saved = cardRepository.save(getCard(request));

        auditService.log(
                AuditAction.CARD_CREATED,
                AuditEntityType.CARD,
                saved.getCardNumber(),
                "ownerUser=" + saved.getOwner().getUniqueKey()
        );

        return mapper.toAdminDto(saved);
    }

    @Transactional
    public void deleteCard(String cardId) {
        Card card = cardRepository.findByCardNumber(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));
        if (!card.getBalance().equals(BigDecimal.ZERO))
            throw new BadBalanceException("Card balance should be zero");

        cardRepository.delete(card);

        auditService.log(
                AuditAction.CARD_DELETED,
                AuditEntityType.CARD,
                card.getCardNumber(),
                "balance=" + card.getBalance()
        );
    }

    @Transactional
    public void changeStatus(String cardNumber, CardStatus status) {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));
        var oldStatus = card.getStatus();

        if (oldStatus == status){
            log.info("Card {} status is already set to {}", cardNumber, status);
            return;
        }

        card.setStatus(status);

        auditService.log(
                AuditAction.CARD_STATUS_CHANGED,
                AuditEntityType.CARD,
                card.getCardNumber(),
                "oldStatus=" + oldStatus + ";newStatus=" + card.getStatus() + ";cardNumber= "+ card.getCardNumber()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminCardDto> getUserCards(UUID userId, Pageable pageable) {

        User user = userRepository.findByUniqueKey(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Page<Card> cards = cardRepository.findAllByOwner(user, pageable);

        return PageMapper.toPageResponse(cards.map(mapper::toAdminDto));
    }

    public Card getCard(AdminCreateCardRequest request) {
        User user = userRepository.findByUniqueKey(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return Card.builder()
                .cardNumber(cardNumberGenerator.generateCardNumber(cardConfig.getPrefix()))
                .owner(user)
                .validityPeriod(request.validityPeriod())
                .status(CardStatus.ACTIVE)
                .balance(request.initialBalance())
                .build();
    }
}
