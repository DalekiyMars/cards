package com.banking.cards.repository;

import com.banking.cards.common.CardStatus;
import com.banking.cards.entity.Card;
import com.banking.cards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findAllByOwner(User owner, Pageable pageable);
    Optional<Card> findByCardNumber(String cardNumber);

    Optional<Card> findByCardNumberAndOwner(String cardNumber, User owner);
    List<Card> findAllByStatusNotAndValidityPeriodBefore(
            CardStatus status,
            LocalDate date
    );
}
