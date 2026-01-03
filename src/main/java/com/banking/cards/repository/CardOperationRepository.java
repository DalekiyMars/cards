package com.banking.cards.repository;

import com.banking.cards.entity.CardOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardOperationRepository extends JpaRepository<CardOperation, Long> {

    Page<CardOperation> findAllByFromCard_IdOrToCard_Id(Long fromId, Long toId, Pageable pageable);
}