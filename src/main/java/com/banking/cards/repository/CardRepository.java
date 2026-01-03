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
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findAllByOwner(User owner, Pageable pageable);

    Optional<Card> findByUniqueKeyAndOwner(UUID uniqueKey, User owner);
    Optional<Card> findByUniqueKey(UUID publicId);
    List<Card> findAllByStatusNotAndValidityPeriodBefore(
            CardStatus status,
            LocalDate date
    );
}
