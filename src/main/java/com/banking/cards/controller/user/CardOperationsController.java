package com.banking.cards.controller.user;

import com.banking.cards.dto.request.CardAmountRequest;
import com.banking.cards.dto.request.CardNumberRequest;
import com.banking.cards.dto.request.CardTransferRequest;
import com.banking.cards.service.user.UserCardOperationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/deposit")
    public void deposit(
            @RequestBody CardNumberRequest cardNumberRequest,
            @Valid @RequestBody CardAmountRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        userCardOperationService.deposit(cardNumberRequest.getCardNumber(), request.amount(), userId);
    }

    @PostMapping("/withdraw")
    public void withdraw(
            @RequestBody CardNumberRequest cardNumberRequest,
            @Valid @RequestBody CardAmountRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        userCardOperationService.withdraw(cardNumberRequest.getCardNumber(), request.amount(), userId);
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
