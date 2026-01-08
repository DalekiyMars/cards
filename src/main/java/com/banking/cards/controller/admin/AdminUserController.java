package com.banking.cards.controller.admin;

import com.banking.cards.dto.request.ChangeUserRoleRequest;
import com.banking.cards.dto.response.ApiErrorResponse;
import com.banking.cards.service.admin.AdminUserService;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(
        name = "Admin Users",
        description = "Управление пользователями администратором"
)
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;
    @Operation(
            summary = "Изменить роль пользователя",
            description = """
                Назначает пользователю новую роль. Доступно только администраторам.
                
                Возможные роли:
                - USER
                - ADMIN
                """
    )
    @ApiResponses({

            // ===== 200 =====
            @ApiResponse(
                    responseCode = "200",
                    description = "Роль пользователя успешно изменена"
            ),

            // ===== 400 =====
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации или некорректный запрос",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = """
                                {
                                  "timestamp": "2026-01-08T12:30:15.123Z",
                                  "status": 400,
                                  "error": "Bad Request",
                                  "message": "role: must not be null",
                                  "path": "/api/admin/users/role"
                                }
                                """)
                    )
            ),

            // ===== 403 =====
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав или бизнес-ограничение",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = """
                                {
                                  "timestamp": "2026-01-08T12:31:10.000Z",
                                  "status": 403,
                                  "error": "Forbidden",
                                  "message": "Access denied",
                                  "path": "/api/admin/users/role"
                                }
                                """)
                    )
            ),

            // ===== 404 =====
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = """
                                {
                                  "timestamp": "2026-01-08T12:32:00.000Z",
                                  "status": 404,
                                  "error": "Not Found",
                                  "message": "User not found",
                                  "path": "/api/admin/users/role"
                                }
                                """)
                    )
            ),

            // ===== 500 =====
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @PatchMapping("/role")
    @ResponseStatus(HttpStatus.OK)
    public void changeUserRole(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Данные для изменения роли",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                {
                                  "id": "550e8400-e29b-41d4-a716-446655440000",
                                  "role": "ADMIN"
                                }
                                """)
                    )
            )
            @RequestBody ChangeUserRoleRequest request
    ) {
        adminUserService.changeUserRole(request.id(), request.role());
    }

}
