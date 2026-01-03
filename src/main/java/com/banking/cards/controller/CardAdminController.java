package com.banking.cards.controller;

import com.banking.cards.common.CardStatus;
import com.banking.cards.dto.CardCreateRequest;
import com.banking.cards.dto.CardDto;
import com.banking.cards.dto.PageResponse;
import com.banking.cards.mapper.PageMapper;
import com.banking.cards.service.admin.CardAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/admin/cards")
@RequiredArgsConstructor
@Tag(
        name = "Admin Cards",
        description = "Административное управление банковскими картами"
)
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class CardAdminController {

    private final CardAdminService cardAdminService;

    // ===== CREATE CARD =====

    @Operation(
            summary = "Создать карту пользователю",
            description = """
                    Создаёт новую банковскую карту для пользователя.
                    
                    Правила:
                    - карта создаётся со статусом ACTIVE
                    - баланс = 0
                    - номер карты уникален
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Карта успешно создана"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "409", description = "Карта с таким номером уже существует")
    })
    @PostMapping
    public CardDto createCard(
            @Valid @RequestBody CardCreateRequest request
    ) {
        return cardAdminService.createCard(request);
    }

    // ===== CHANGE CARD STATUS =====

    @Operation(
            summary = "Изменить статус карты",
            description = "Блокирует, активирует или помечает карту как устаревшую"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Статус карты изменён"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    @PatchMapping("/{id}/status")
    public void changeCardStatus(
            @PathVariable Long id,
            @RequestParam CardStatus status
    ) {
        cardAdminService.changeStatus(id, status);
    }

    // ===== DELETE CARD =====

    @Operation(
            summary = "Удалить карту",
            description = "Удаляет карту из системы"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Карта удалена"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable Long id) {
        cardAdminService.deleteCard(id);
    }

    // ===== GET ALL CARDS =====

    @Operation(
            summary = "Получить все карты",
            description = "Возвращает список всех карт в системе с пагинацией"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список карт")
    })
    @GetMapping
    public PageResponse<CardDto> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                Math.min(size, 50),
                Sort.by("id").ascending()
        );

        return cardAdminService.getAllCards(pageable);
    }
}
