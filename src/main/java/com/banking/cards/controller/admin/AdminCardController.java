package com.banking.cards.controller.admin;

import com.banking.cards.common.CardStatus;
import com.banking.cards.dto.request.AdminCreateCardRequest;
import com.banking.cards.dto.request.CardNumberRequest;
import com.banking.cards.dto.response.AdminCardDto;
import com.banking.cards.dto.response.ApiErrorResponse;
import com.banking.cards.dto.response.PageResponse;
import com.banking.cards.service.admin.AdminCardService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/cards")
@RequiredArgsConstructor
@Tag(
        name = "Admin Cards",
        description = "Управление банковскими картами пользователей администратором"
)
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminCardController {

    private final AdminCardService cardAdminService;

    // ===== CREATE CARD =====

    @Operation(
            summary = "Создать банковскую карту пользователю",
            description = """
                    Создаёт новую банковскую карту для пользователя.
                    
                    Правила:
                    - статус карты: ACTIVE
                    - номер карты генерируется автоматически
                    - баланс задаётся при создании
                    """
    )
    @ApiResponses({

            @ApiResponse(
                    responseCode = "200",
                    description = "Карта успешно создана",
                    content = @Content(
                            schema = @Schema(implementation = AdminCardDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "cardNumber": "4276550012345678",
                                      "status": "ACTIVE",
                                      "balance": 0.00,
                                      "validityPeriod": "2029-12",
                                      "ownerId": "550e8400-e29b-41d4-a716-446655440000"
                                    }
                                    """)
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "User not found",
                                      "path": "/api/admin/cards"
                                    }
                                    """)
                    )
            ),

            @ApiResponse(
                    responseCode = "409",
                    description = "Конфликт при создании карты",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @PostMapping
    public AdminCardDto createCard(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания карты",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(value = """
                                    {
                                      "userId": "550e8400-e29b-41d4-a716-446655440000",
                                      "validityPeriod": "2029-12",
                                      "initialBalance": 0.00
                                    }
                                    """)
                    )
            )
            @RequestBody AdminCreateCardRequest request
    ) {
        return cardAdminService.createCard(request);
    }

    // ===== CHANGE CARD STATUS =====

    @Operation(
            summary = "Изменить статус карты",
            description = """
                    Изменяет статус карты.
                    
                    Возможные значения:
                    - ACTIVE
                    - BLOCKED
                    - EXPIRED
                    """
    )
    @ApiResponses({

            @ApiResponse(
                    responseCode = "202",
                    description = "Статус карты успешно изменён"
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Карта не найдена",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping("/status")
    public void changeCardStatus(
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

            @Parameter(
                    description = "Новый статус карты",
                    example = "BLOCKED"
            )
            @RequestParam CardStatus status
    ) {
        cardAdminService.changeStatus(cardNumberRequest.getCardNumber(), status);
    }

    // ===== DELETE CARD =====

    @Operation(
            summary = "Удалить банковскую карту",
            description = """
                    Удаляет карту из системы.
                    
                    Условие:
                    - баланс карты должен быть равен 0
                    """
    )
    @ApiResponses({

            @ApiResponse(
                    responseCode = "200",
                    description = "Карта успешно удалена"
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Баланс карты не равен 0",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Карта не найдена",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public void deleteCard(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Номер карты для удаления",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(value = """
                                    {
                                      "cardNumber": "4276550012345678"
                                    }
                                    """)
                    )
            )
            @RequestBody CardNumberRequest cardNumberRequest
    ) {
        cardAdminService.deleteCard(cardNumberRequest.getCardNumber());
    }

    // ===== GET ALL CARDS =====

    @Operation(
            summary = "Получить карты пользователя",
            description = "Возвращает список всех карт пользователя с пагинацией"
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
                    responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @GetMapping("{uuid}")
    public PageResponse<AdminCardDto> getAllUserCards(
            @Parameter(
                    description = "UUID пользователя",
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable UUID uuid,

            @Parameter(description = "Номер страницы", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы (макс. 50)", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                Math.min(size, 50),
                Sort.by("id").ascending()
        );
        return cardAdminService.getUserCards(uuid, pageable);
    }
}
