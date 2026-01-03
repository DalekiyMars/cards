package com.banking.cards.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BadBalanceException extends RuntimeException {
    private final String message;
    public BadBalanceException(String message) {
        this.message = message;
    }
}
