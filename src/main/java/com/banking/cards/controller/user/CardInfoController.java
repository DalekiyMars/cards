package com.banking.cards.controller.user;

import com.banking.cards.dto.request.CardNumberRequest;
import com.banking.cards.dto.response.CardDto;
import com.banking.cards.dto.response.CardOperationDto;
import com.banking.cards.dto.response.PageResponse;
import com.banking.cards.service.user.UserCardInfoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/cards/info")
@RequiredArgsConstructor
@Tag(name = "Cards")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class CardInfoController {

    private final UserCardInfoService cardService;

    // ===== GET ALL CARDS =====
    @GetMapping
    public PageResponse<CardDto> getMyCards(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return cardService.getUserCards(userId, page, size);
    }

    // ===== GET ONE CARD =====
    @GetMapping("/one")
    public CardDto getMyCard(
            @RequestBody CardNumberRequest cardNumberRequest,
            @AuthenticationPrincipal UUID userId
    ) {
        return cardService.getUserCard(cardNumberRequest.getCardNumber(), userId);
    }

    // ===== CARD OPERATIONS =====
    @GetMapping("/operations")
    public PageResponse<CardOperationDto> getCardOperations(
            @RequestBody CardNumberRequest cardNumberRequest,
            @AuthenticationPrincipal UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return cardService.getCardOperations(cardNumberRequest.getCardNumber(), userId, page, size);
    }
}
