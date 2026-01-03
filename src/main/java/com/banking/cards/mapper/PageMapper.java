package com.banking.cards.mapper;

import com.banking.cards.dto.CardDto;
import com.banking.cards.dto.CardOperationDto;
import com.banking.cards.dto.PageResponse;
import org.springframework.data.domain.Page;

public final class PageMapper {
    public static PageResponse<CardDto> toPageResponseCard(Page<CardDto> cardOperations, int page, int size) {
        return new PageResponse<>(cardOperations.stream().toList(), page, size, cardOperations.getSize());
    }
    public static PageResponse<CardOperationDto> toPageResponseCardOperation(Page<CardOperationDto> cardOperations, int page, int size) {
        return new PageResponse<>(cardOperations.stream().toList(), page, size, cardOperations.getSize());
    }
}
