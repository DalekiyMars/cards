package com.banking.cards.controller;

import com.banking.cards.dto.CardAmountRequest;
import com.banking.cards.dto.CardTransferRequest;
import com.banking.cards.service.user.UserCardOperationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(
        name = "Cards",
        description = "Операции с банковскими картами"
)
@SecurityRequirement(name = "bearerAuth")
public class CardOperationsController {

    private final UserCardOperationService userCardOperationService;

    @PostMapping("/{id}/deposit")
    public void deposit(
            @PathVariable Long id,
            @Valid @RequestBody CardAmountRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        userCardOperationService.deposit(id, request.amount(), userId);
    }

    @PostMapping("/{id}/withdraw")
    public void withdraw(
            @PathVariable Long id,
            @Valid @RequestBody CardAmountRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        userCardOperationService.withdraw(id, request.amount(), userId);
    }

    @PostMapping("/transfer")
    public void transfer(
            @Valid @RequestBody CardTransferRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        userCardOperationService.transfer(
                request.fromCardId(),
                request.toCardId(),
                request.amount(),
                userId
        );
    }
}
