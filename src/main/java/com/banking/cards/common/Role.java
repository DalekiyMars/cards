package com.banking.cards.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Роль пользователя",
        example = "ADMIN"
)
public enum Role {
    USER,
    ADMIN
}
