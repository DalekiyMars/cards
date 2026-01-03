package com.banking.cards.service.user;

import com.banking.cards.dto.CardDto;
import com.banking.cards.dto.CardOperationDto;
import com.banking.cards.dto.PageResponse;
import com.banking.cards.entity.Card;
import com.banking.cards.entity.CardOperation;
import com.banking.cards.entity.User;
import com.banking.cards.mapper.CardMapper;
import com.banking.cards.mapper.PageMapper;
import com.banking.cards.repository.CardOperationRepository;
import com.banking.cards.repository.CardRepository;
import com.banking.cards.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserCardInfoService {

    private final CardRepository cardRepository;
    private final CardOperationRepository operationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<CardDto> getUserCards(Long userId, int page, int size) {
        User user = getUser(userId);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("id").ascending()
        );

        Page<CardDto> cards = cardRepository.findAllByOwner(user, pageable).map(CardMapper::toDto);

        return PageMapper.toPageResponse(cards);
    }

    @Transactional(readOnly = true)
    public CardDto getUserCard(Long cardId, Long userId) {
        User user = getUser(userId);

        Card card = cardRepository.findByIdAndOwner(cardId, user)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));

        return CardMapper.toDto(card);
    }

    @Transactional(readOnly = true)
    public PageResponse<CardOperationDto> getCardOperations(
            Long cardId,
            Long userId,
            int page,
            int size
    ) {
        User user = getUser(userId);

        Card card = cardRepository.findByIdAndOwner(cardId, user)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<CardOperationDto> operations =
                operationRepository.findAllByFromCard_IdOrToCard_Id(
                        card.getId(),
                        card.getId(),
                        pageable
                ).map(CardMapper::toOperationDto);

        return PageMapper.toPageResponse(operations);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}
