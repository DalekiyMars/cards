package com.banking.cards.controller;

import com.banking.cards.dto.CardDto;
import com.banking.cards.dto.CardOperationDto;
import com.banking.cards.dto.PageResponse;
import com.banking.cards.service.user.UserCardInfoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "Cards")
@SecurityRequirement(name = "bearerAuth")
public class CardInfoController {

    private final UserCardInfoService cardService;

    // ===== GET ALL CARDS =====
    @GetMapping
    public PageResponse<CardDto> getMyCards(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return cardService.getUserCards(userId, page, size);
    }

    // ===== GET ONE CARD =====
    @GetMapping("/{id}")
    public CardDto getMyCard(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        return cardService.getUserCard(id, userId);
    }

    // ===== CARD OPERATIONS =====
    @GetMapping("/{id}/operations")
    public PageResponse<CardOperationDto> getCardOperations(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return cardService.getCardOperations(id, userId, page, size);
    }
}
