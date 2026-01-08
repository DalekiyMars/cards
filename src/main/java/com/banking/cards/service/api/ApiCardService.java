package com.banking.cards.service.api;

import com.banking.cards.common.audit.AuditAction;
import com.banking.cards.common.audit.AuditEntityType;
import com.banking.cards.dto.request.AdminCreateCardRequest;
import com.banking.cards.dto.response.CardDto;
import com.banking.cards.dto.response.PageResponse;
import com.banking.cards.entity.Card;
import com.banking.cards.entity.User;
import com.banking.cards.mapper.CardMapper;
import com.banking.cards.mapper.PageMapper;
import com.banking.cards.repository.CardRepository;
import com.banking.cards.repository.UserRepository;
import com.banking.cards.service.AuditService;
import com.banking.cards.service.admin.AdminCardService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiCardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper mapper;
    private final AuditService auditService;
    private final AdminCardService adminCardService;

    @Transactional(readOnly = true)
    public PageResponse<CardDto> getUserCards(UUID userId, Pageable pageable) {

        User user = userRepository.findByUniqueKey(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Page<Card> cards = cardRepository.findAllByOwner(user, pageable);

        return PageMapper.toPageResponse(cards.map(mapper::toDto));
    }

    @Transactional
    public CardDto createCard(AdminCreateCardRequest request) {
        Card saved = cardRepository.save(adminCardService.saveCard(request));
        auditService.log(
                AuditAction.CARD_CREATED,
                AuditEntityType.CARD,
                saved.getCardNumber(),
                "ownerUser=" + saved.getOwner().getUniqueKey()+
                        "generated card by side service"
        );

        return mapper.toDto(saved);
    }

}
