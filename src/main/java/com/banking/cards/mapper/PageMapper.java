package com.banking.cards.mapper;

import com.banking.cards.dto.PageResponse;
import org.springframework.data.domain.Page;

public final class PageMapper {
    public static <T> PageResponse<T> toPageResponse(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
