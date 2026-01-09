package com.banking.cards.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class CardNumberGenerator {
    public String generateCardNumber(String prefix) {
        Random random = new Random();
        StringBuilder cardNumber = new StringBuilder(prefix);

        // Генерируем до 15 цифр (16-я будет контрольная)
        while (cardNumber.length() < 16) {
            cardNumber.append(random.nextInt(10));
        }

        return cardNumber.toString();
    }
}
