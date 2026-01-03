package com.banking.cards.service.user;

import com.banking.cards.common.CardStatus;
import com.banking.cards.entity.Card;
import com.banking.cards.repository.CardRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CardBlockService {

    private final CardRepository cardRepository;

    @Transactional
    public void requestBlock(Long userId, Long cardId) {
        Card card = cardRepository.findByIdAndOwnerId(cardId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));

        if (Objects.equals(card.getStatus(), CardStatus.BLOCKED)) {
            return;
        }

        card.setStatus(CardStatus.BLOCKED);
    }
}
