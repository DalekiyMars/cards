package com.banking.cards.dto.request;

import com.banking.cards.common.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(
        description = "Запрос на изменение роли пользователя"
)
public record ChangeUserRoleRequest(

        @Schema(
                description = "UUID пользователя",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        @NotNull
        UUID id,

        @Schema(
                description = "Новая роль пользователя",
                allowableValues = {"USER", "ADMIN"},
                example = "ADMIN"
        )
        @NotNull
        Role role
) {}
