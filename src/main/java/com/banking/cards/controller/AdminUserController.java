package com.banking.cards.controller;

import com.banking.cards.dto.ChangeUserRoleRequest;
import com.banking.cards.service.admin.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(
        name = "Admin Users",
        description = "Административное управление пользователями"
)
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(
            summary = "Изменить роль пользователя",
            description = """
                    Назначает пользователю новую роль.
                    
                    Возможные роли:
                    - USER
                    - ADMIN
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Роль успешно изменена"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @PatchMapping("/{id}/role")
    public void changeUserRole(
            @PathVariable Long id,
            @Valid @RequestBody ChangeUserRoleRequest request
    ) {
        adminUserService.changeUserRole(id, request.role());
    }
}
