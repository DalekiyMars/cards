package com.banking.cards.dto;

import com.banking.cards.common.Role;
import jakarta.validation.constraints.NotNull;

public record ChangeUserRoleRequest(
        @NotNull
        Role role
) {
}
