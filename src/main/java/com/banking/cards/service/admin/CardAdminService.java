package com.banking.cards.service.admin;

import com.banking.cards.common.CardStatus;
import com.banking.cards.dto.CardCreateRequest;
import com.banking.cards.dto.CardDto;
import com.banking.cards.dto.PageResponse;
import com.banking.cards.entity.Card;
import com.banking.cards.entity.User;
import com.banking.cards.mapper.CardMapper;
import com.banking.cards.mapper.PageMapper;
import com.banking.cards.repository.CardRepository;
import com.banking.cards.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CardAdminService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @Transactional
    public CardDto createCard(CardCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Card card = Card.builder()
                .cardNumber(request.cardNumber())
                .owner(user)
                .validityPeriod(request.validityPeriod())
                .status(CardStatus.ACTIVE)
                .balance(request.initialBalance())
                .build();

        return CardMapper.toDto(cardRepository.save(card));
    }

    @Transactional
    public void changeStatus(Long cardId, CardStatus status) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));
        if (Objects.equals(card.getStatus(), status)) {
            return;
        }
        card.setStatus(status);
    }

    @Transactional
    public void deleteCard(Long cardId) {
        cardRepository.deleteById(cardId);
    }

    public PageResponse<CardDto> getAllCards(Pageable pageable) {
        var cards = cardRepository.findAll(pageable)
                .map(CardMapper::toDto);
        return PageMapper.toPageResponse(cards);
    }
}
