package com.banking.cards.common.audit;

public enum AuditAction {
    CARD_CREATED,
    CARD_DELETED,
    CARD_STATUS_CHANGED,

    CARD_DEPOSIT,
    CARD_WITHDRAW,
    CARD_TRANSFER_OUT,
    CARD_TRANSFER_IN,

    USER_REGISTERED,
    USER_ROLE_CHANGED,
    USER_LOGIN
}
