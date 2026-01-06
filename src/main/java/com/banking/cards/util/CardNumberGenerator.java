package com.banking.cards.util;

import java.util.UUID;

public class CardNumberGenerator {
    public static String generateCardNumber() {
        return UUID.randomUUID().toString()
                .replaceAll("\\D", "")
                .substring(0, 16);
    }
}
