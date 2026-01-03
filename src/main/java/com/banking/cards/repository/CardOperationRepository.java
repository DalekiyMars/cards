package com.banking.cards.repository;

import com.banking.cards.entity.CardOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardOperationRepository extends JpaRepository<CardOperation, Long> {

    List<CardOperation> findAllByFromCard_IdOrToCard_Id(Long fromId, Long toId);
}