package com.banking.cards.service;

import com.banking.cards.common.CardStatus;
import com.banking.cards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;

@Service
@Slf4j
@RequiredArgsConstructor
public class CardStatusService {

    private final CardRepository cardRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void markExpiredCards() {
        var expiredCards = cardRepository
                .findAllByStatusNotAndValidityPeriodBefore(
                        CardStatus.EXPIRED,
                        YearMonth.now()
                );

        expiredCards.forEach(card ->
                card.setStatus(CardStatus.EXPIRED)
        );
        log.info("Marked {} cards as OUTDATE", expiredCards.size());
    }
}