package com.banking.cards.service.admin;

import com.banking.cards.common.CardStatus;
import com.banking.cards.common.audit.AuditAction;
import com.banking.cards.common.audit.AuditEntityType;
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
    private final AuditService auditService;
    private final CardMapper mapper;

    @Transactional
    public AdminCardDto createCard(AdminCreateCardRequest request) {

        User user = userRepository.findByUniqueKey(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Card card = Card.builder()
                .cardNumber(CardNumberGenerator.generateCardNumber())
                .owner(user)
                .validityPeriod(request.validityPeriod())
                .status(CardStatus.ACTIVE)
                .balance(request.initialBalance())
                .build();

        Card saved = cardRepository.save(card);
        auditService.log(
                AuditAction.CARD_CREATED,
                AuditEntityType.CARD,
                card.getUniqueKey(),
                "ownerUser=" + card.getOwner().getUniqueKey()
        );

        return mapper.toAdminDto(saved);
    }

    @Transactional
    public void deleteCard(UUID cardId) {
        Card card = cardRepository.findByUniqueKey(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));
        if (!card.getBalance().equals(BigDecimal.ZERO))
            throw new BadBalanceException("Card balance should be zero");

        cardRepository.delete(card);

        auditService.log(
                AuditAction.CARD_DELETED,
                AuditEntityType.CARD,
                card.getUniqueKey(),
                "balance=" + card.getBalance()
        );
    }

    @Transactional
    public void changeStatus(UUID cardId, CardStatus status) {
        Card card = cardRepository.findByUniqueKey(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));
        var oldStatus = card.getStatus();

        card.setStatus(status);

        auditService.log(
                AuditAction.CARD_STATUS_CHANGED,
                AuditEntityType.CARD,
                card.getUniqueKey(),
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
}
