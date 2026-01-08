package com.banking.cards.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Статус банковской карты",
        example = "ACTIVE"
)
public enum CardStatus {
    ACTIVE,
    BLOCKED,
    EXPIRED
}
