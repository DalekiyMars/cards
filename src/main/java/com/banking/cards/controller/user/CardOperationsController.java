package com.banking.cards.controller.user;

import com.banking.cards.dto.request.CardNumberAndAmountRequest;
import com.banking.cards.dto.request.CardTransferRequest;
import com.banking.cards.dto.response.ApiErrorResponse;
import com.banking.cards.service.user.UserCardOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(
        name = "Cards",
        description = "Операции с банковскими картами"
)
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class CardOperationsController {

    private final UserCardOperationService userCardOperationService;

    @Operation(
            summary = "Пополнить карту",
            description = """
                    Вносит средства на карту текущего пользователя.
                    
                    ❗ Условия:
                    - карта должна принадлежать пользователю
                    - сумма должна быть положительной
                    """
    )
    @ApiResponses({

            @ApiResponse(
                    responseCode = "200",
                    description = "Карта успешно пополнена"
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректная сумма или карта не принадлежит пользователю",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Invalid amount or card does not belong to user",
                                      "path": "/api/cards/deposit"
                                    }
                                    """)
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Карта не найдена",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещён",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @PostMapping("/deposit")
    public void deposit(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для пополнения карты",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(value = """
                                    {
                                      "cardNumber": "4276550012345678",
                                      "amount": 1000.00
                                    }
                                    """)
                    )
            )
            @RequestBody CardNumberAndAmountRequest request,
            @AuthenticationPrincipal String userId
    ) {
        userCardOperationService.deposit(request.cardNumber(), request.amount(), UUID.fromString(userId));
    }

    @Operation(
            summary = "Снять средства с карты",
            description = """
                    Списывает средства с карты текущего пользователя.
                    
                    ❗ Условия:
                    - карта должна принадлежать пользователю
                    - на карте должно быть достаточно средств
                    """
    )
    @ApiResponses({

            @ApiResponse(
                    responseCode = "200",
                    description = "Средства успешно списаны"
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Недостаточно средств или некорректная сумма",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Insufficient balance",
                                      "path": "/api/cards/withdraw"
                                    }
                                    """)
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Карта не найдена",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Карта не принадлежит пользователю",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @PostMapping("/withdraw")
    public void withdraw(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для списания средств",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(value = """
                                    {
                                      "cardNumber": "4276550012345678",
                                      "amount": 500.00
                                    }
                                    """)
                    )
            )
            @RequestBody CardNumberAndAmountRequest request,
            @AuthenticationPrincipal String userId
    ) {
        userCardOperationService.withdraw(request.cardNumber(), request.amount(), UUID.fromString(userId));
    }

    @Operation(
            summary = "Перевод средств между картами",
            description = """
                    Переводит средства с одной карты пользователя на другую карту.
                    
                    ❗ Условия:
                    - карта отправителя принадлежит пользователю
                    - баланс карты достаточен
                    """
    )
    @ApiResponses({

            @ApiResponse(
                    responseCode = "200",
                    description = "Перевод успешно выполнен"
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные или недостаточно средств",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Одна из карт не найдена",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Карта отправителя не принадлежит пользователю",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @PostMapping("/transfer")
    public void transfer(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для перевода средств",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(value = """
                                    {
                                      "fromCardId": "4276550012345678",
                                      "toCardId": "4276550098765432",
                                      "amount": 250.00
                                    }
                                    """)
                    )
            )
            @RequestBody CardTransferRequest request,
            @AuthenticationPrincipal String userId
    ) {
        userCardOperationService.transfer(
                request.fromCardId(),
                request.toCardId(),
                request.amount(),
                UUID.fromString(userId)
        );
    }
}
