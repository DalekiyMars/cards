package com.banking.cards.controller;

import com.banking.cards.dto.CardAmountRequest;
import com.banking.cards.dto.CardTransferRequest;
import com.banking.cards.entity.User;
import com.banking.cards.service.user.CardService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
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
public class CardController {

    private final CardService cardService;

    // ===== DEPOSIT =====

    @Operation(
            summary = "Пополнение карты",
            description = """
                    Пополняет баланс указанной карты.
                    
                    Ограничения:
                    -> карта должна принадлежать пользователю
                    -> карта должна быть ACTIVE
                    -> сумма должна быть больше 0
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Баланс успешно пополнен"),
            @ApiResponse(responseCode = "400", description = "Некорректная сумма"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Карта не принадлежит пользователю"),
            @ApiResponse(responseCode = "409", description = "Карта заблокирована или просрочена")
    })
    @PostMapping("/{id}/deposit")
    public void deposit(
            @Parameter(description = "ID карты", example = "1")
            @PathVariable Long id,

            @Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Сумма пополнения",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CardAmountRequest.class)
                    )
            )
            CardAmountRequest request,

            @AuthenticationPrincipal User user
    ) {
        cardService.deposit(id, request.amount(), user);
    }

    // ===== WITHDRAW =====

    @Operation(
            summary = "Списание средств",
            description = """
                    Списывает средства с карты.
                    
                    Ограничения:
                    -> карта должна принадлежать пользователю
                    -> карта должна быть ACTIVE
                    -> на карте должно быть достаточно средств
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Средства успешно списаны"),
            @ApiResponse(responseCode = "400", description = "Некорректная сумма"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Карта не принадлежит пользователю"),
            @ApiResponse(responseCode = "409", description = "Недостаточно средств / карта заблокирована")
    })
    @PostMapping("/{id}/withdraw")
    public void withdraw(
            @Parameter(description = "ID карты", example = "1")
            @PathVariable Long id,

            @Valid
            @RequestBody
            CardAmountRequest request,

            @AuthenticationPrincipal User user
    ) {
        cardService.withdraw(id, request.amount(), user);
    }

    // ===== TRANSFER =====

    @Operation(
            summary = "Перевод между картами",
            description = """
                    Переводит средства между двумя картами пользователя.
                    
                    Ограничения:
                    -> обе карты должны принадлежать пользователю
                    -> обе карты должны быть ACTIVE
                    -> на исходной карте должно быть достаточно средств
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Перевод успешно выполнен"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Одна из карт не принадлежит пользователю"),
            @ApiResponse(responseCode = "409", description = "Недостаточно средств / карта заблокирована")
    })
    @PostMapping("/transfer")
    public void transfer(
            @Valid
            @RequestBody
            CardTransferRequest request,

            @AuthenticationPrincipal User user
    ) {
        cardService.transfer(
                request.fromCardId(),
                request.toCardId(),
                request.amount(),
                user
        );
    }
}
