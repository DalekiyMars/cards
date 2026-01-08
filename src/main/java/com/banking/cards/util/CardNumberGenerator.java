package com.banking.cards.util;

import java.util.Random;

public class CardNumberGenerator {
    public static String generateCardNumber(String prefix) {
        Random random = new Random();
        StringBuilder cardNumber = new StringBuilder(prefix);

        // Генерируем до 15 цифр (16-я будет контрольная)
        while (cardNumber.length() < 15) {
            cardNumber.append(random.nextInt(10));
        }

        // Вычисляем контрольную цифру по алгоритму Луна
        int checkDigit = calculateLuhnCheckDigit(cardNumber.toString());
        cardNumber.append(checkDigit);

        return cardNumber.toString();
    }

    private static int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = true;

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = digit - 9;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (10 - (sum % 10)) % 10;
    }
}
