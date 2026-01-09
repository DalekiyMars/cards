package com.banking.cards.controller.user;

import com.banking.cards.dto.request.CardNumberRequest;
import com.banking.cards.dto.response.ApiErrorResponse;
import com.banking.cards.dto.response.CardDto;
import com.banking.cards.dto.response.CardOperationDto;
import com.banking.cards.dto.response.PageResponse;
import com.banking.cards.service.user.UserCardInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/cards/info")
@RequiredArgsConstructor
@Tag(
        name = "Cards",
        description = "Просмотр информации по банковским картам пользователя"
)
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class CardInfoController {

    private final UserCardInfoService cardService;

    // ===== GET ALL CARDS =====

    @Operation(
            summary = "Получить все карты пользователя",
            description = "Возвращает список всех банковских карт текущего пользователя с пагинацией"
    )
    @ApiResponses({

            @ApiResponse(
                    responseCode = "200",
                    description = "Список карт пользователя",
                    content = @Content(
                            schema = @Schema(implementation = PageResponse.class)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Пользователь не авторизован",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @GetMapping
    public PageResponse<CardDto> getMyCards(
            @AuthenticationPrincipal UUID userId,

            @Parameter(description = "Номер страницы", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        return cardService.getUserCards(userId, page, size);
    }

    // ===== GET ONE CARD =====

    @Operation(
            summary = "Получить одну карту пользователя",
            description = "Возвращает информацию по конкретной карте текущего пользователя"
    )
    @ApiResponses({

            @ApiResponse(
                    responseCode = "200",
                    description = "Информация по карте",
                    content = @Content(
                            schema = @Schema(implementation = CardDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "cardNumber": "4276550012345678",
                                      "status": "ACTIVE",
                                      "balance": 1500.00,
                                      "validityPeriod": "2029-12"
                                    }
                                    """)
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Карта не найдена или не принадлежит пользователю",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Card not found",
                                      "path": "/api/cards/info/one"
                                    }
                                    """)
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
    @PostMapping("/one")
    public CardDto getMyCard(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Номер карты",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(value = """
                                    {
                                      "cardNumber": "4276550012345678"
                                    }
                                    """)
                    )
            )
            @RequestBody CardNumberRequest cardNumberRequest,
            @AuthenticationPrincipal UUID userId
    ) {
        return cardService.getUserCard(cardNumberRequest.getCardNumber(), userId);
    }

    // ===== CARD OPERATIONS =====

    @Operation(
            summary = "Получить операции по карте",
            description = "Возвращает историю операций по карте пользователя с пагинацией"
    )
    @ApiResponses({

            @ApiResponse(
                    responseCode = "200",
                    description = "Список операций по карте",
                    content = @Content(
                            schema = @Schema(implementation = PageResponse.class)
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Карта не найдена или не принадлежит пользователю",
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
    @PostMapping("/operations")
    public PageResponse<CardOperationDto> getCardOperations(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Номер карты",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(value = """
                                    {
                                      "cardNumber": "4276550012345678"
                                    }
                                    """)
                    )
            )
            @RequestBody CardNumberRequest cardNumberRequest,
            @AuthenticationPrincipal UUID userId,
            @Parameter(description = "Номер страницы", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        return cardService.getCardOperations(cardNumberRequest.getCardNumber(), userId, page, size);
    }
}
