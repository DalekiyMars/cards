package com.banking.cards.controller;

import com.banking.cards.constants.TestCardNumbers;
import com.banking.cards.constants.TestConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.banking.cards.constants.TestConstants.CARD_PATTERN;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CardPatternValidationTest {
    // Паттерн из Constants.java
    private static final Pattern PATTERN = Pattern.compile(CARD_PATTERN);

    @Test
    void testPatternMatchesKnownValidCards() {
        // Проверяем несколько заведомо валидных карт
        assertThat(PATTERN.matcher("4111111111111111").matches()).isTrue();
        assertThat(PATTERN.matcher("5555555555554444").matches()).isTrue();
        assertThat(PATTERN.matcher("4111-1111-1111-1111").matches()).isTrue();
        assertThat(PATTERN.matcher("4111 1111 1111 1111").matches()).isTrue();
        assertThat(PATTERN.matcher("378282246310005").matches()).isTrue(); // 15 цифр
    }

    @Test
    void testPatternRejectsInvalidCards() {
        assertThat(PATTERN.matcher("").matches()).isFalse();
        assertThat(PATTERN.matcher("1234").matches()).isFalse();
        assertThat(PATTERN.matcher("ABCD-EFGH-IJKL-MNOP").matches()).isFalse();
        assertThat(PATTERN.matcher("4111 1111 1111 111").matches()).isFalse(); // 15 цифр с пробелами
    }

    @ParameterizedTest
    @MethodSource("provideAllTestCardNumbers")
    void testAllTestCardNumbersAgainstPattern(String cardNumber, String description) {
        System.out.printf("Проверка: %-30s (%s) -> ", cardNumber, description);

        boolean matches = PATTERN.matcher(cardNumber).matches();
        System.out.println(matches ? "СООТВЕТСТВУЕТ" : "НЕ СООТВЕТСТВУЕТ");

        // Анализируем, почему не соответствует
        if (!matches) {
            analyzePatternMismatch(cardNumber);
        }
    }

    private void analyzePatternMismatch(String cardNumber) {
        // Удаляем все нецифровые символы
        String digitsOnly = cardNumber.replaceAll("[^0-9]", "");

        System.out.println("  Анализ:");
        System.out.println("    Только цифры: " + digitsOnly);
        System.out.println("    Длина: " + digitsOnly.length());

        // Проверяем первую часть паттерна
        boolean firstPart = cardNumber.matches(TestConstants.FIRST_CARD_PATTERN_PART);
        System.out.println("    Соответствует первой части (16 цифр с разделителями): " + firstPart);

        // Проверяем вторую часть паттерна: ^\d{13,19}$
        boolean secondPart = digitsOnly.matches(TestConstants.SECOND_CARD_PATTERN_PART);
        System.out.println("    Соответствует второй части (13-19 цифр без разделителей): " + secondPart);

        // Проверяем форматирование
        if (cardNumber.contains(" ") || cardNumber.contains("-")) {
            String[] groups = cardNumber.split("[- ]+");
            System.out.println("    Групп после разделения: " + groups.length);
            for (int i = 0; i < groups.length; i++) {
                System.out.println("      Группа " + i + ": '" + groups[i] + "' (длина: " + groups[i].length() + ")");
            }
        }
    }

    @Test
    void comprehensiveTestCardNumbersAnalysis() {
        System.out.println("\n=== АНАЛИЗ ВСЕХ ТЕСТОВЫХ НОМЕРОВ КАРТ ===\n");

        int total = TestCardNumbers.testNumbers.length;
        int matches = 0;
        int firstPartMatches = 0;
        int secondPartMatches = 0;

        for (String cardNumber : TestCardNumbers.testNumbers) {
            System.out.printf("%-35s -> ", cardNumber);

            boolean matchesPattern = PATTERN.matcher(cardNumber).matches();
            String digitsOnly = cardNumber.replaceAll("[^0-9]", "");

            boolean firstPart = cardNumber.matches(TestConstants.FIRST_CARD_PATTERN_PART);
            boolean secondPart = digitsOnly.matches(TestConstants.SECOND_CARD_PATTERN_PART);

            if (matchesPattern) {
                matches++;
                System.out.print("ДА ");
                if (firstPart) {
                    firstPartMatches++;
                    System.out.print("(первая часть) ");
                } else if (secondPart) {
                    secondPartMatches++;
                    System.out.print("(вторая часть) ");
                }
            } else {
                System.out.print("НЕТ");

                if (!firstPart && !secondPart) {
                    System.out.print(" (не соответствует ни одной части)");
                }
            }

            System.out.println(" [цифры: " + digitsOnly + ", длина: " + digitsOnly.length() + "]");
        }

        System.out.println("\n=== СТАТИСТИКА ===");
        System.out.println("Всего номеров: " + total);
        System.out.println("Соответствуют паттерну: " + matches + " (" + (matches * 100 / total) + "%)");
        System.out.println("  - по первой части (16 цифр с разделителями): " + firstPartMatches);
        System.out.println("  - по второй части (13-19 цифр без разделителей): " + secondPartMatches);
        System.out.println("Не соответствуют: " + (total - matches) + " (" + ((total - matches) * 100 / total) + "%)");
    }

    @Test
    void identifyProblematicCards() {
        System.out.println("\n=== ПРОБЛЕМНЫЕ НОМЕРА КАРТ ===");

        for (String cardNumber : TestCardNumbers.testNumbers) {
            if (!PATTERN.matcher(cardNumber).matches()) {
                System.out.println("\nПроблемный номер: '" + cardNumber + "'");

                String digitsOnly = cardNumber.replaceAll("[^0-9]", "");
                int digitCount = digitsOnly.length();

                System.out.println("  Только цифры: '" + digitsOnly + "'");
                System.out.println("  Количество цифр: " + digitCount);

                if (digitCount < 13 || digitCount > 19) {
                    System.out.println("  ПРОБЛЕМА: количество цифр " + digitCount + " вне диапазона 13-19");
                }

                if (cardNumber.contains(" ") || cardNumber.contains("-")) {
                    // Проверяем формат групп
                    String[] groups = cardNumber.split("[- ]+");
                    if (groups.length != 4) {
                        System.out.println("  ПРОБЛЕМА: " + groups.length + " групп вместо 4");
                    }

                    for (int i = 0; i < groups.length; i++) {
                        if (groups[i].length() != 4) {
                            System.out.println("  ПРОБЛЕМА: группа " + i + " имеет длину " + groups[i].length() + " вместо 4");
                        }
                        if (!groups[i].matches("\\d{4}")) {
                            System.out.println("  ПРОБЛЕМА: группа " + i + " содержит не только цифры: '" + groups[i] + "'");
                        }
                    }

                    // Проверяем, что нет лишних символов в конце
                    if (!cardNumber.matches("^\\d{4}([- ]?\\d{4}){3}$")) {
                        System.out.println("  ПРОБЛЕМА: неправильный формат разделителей");
                    }
                }
            }
        }
    }

    private static Stream<Object[]> provideAllTestCardNumbers() {
        return Arrays.stream(TestCardNumbers.testNumbers)
                .map(card -> new Object[] {card, getCardDescription(card)});
    }

    private static String getCardDescription(String cardNumber) {
        String digitsOnly = cardNumber.replaceAll("[^0-9]", "");
        int length = digitsOnly.length();

        if (cardNumber.contains(" ")) {
            return length + " цифр с пробелами";
        } else if (cardNumber.contains("-")) {
            return length + " цифр с дефисами";
        } else {
            return length + " цифр без разделителей";
        }
    }

    @Test
    void suggestPatternImprovements() {
        System.out.println("\n=== ПРЕДЛОЖЕНИЯ ПО УЛУЧШЕНИЮ ПАТТЕРНА ===");

        System.out.println("\nТекущий паттерн: " + CARD_PATTERN);
        System.out.println("\nПроблемы:");
        System.out.println("1. Вторая часть (^\\d{13,19}$) требует, чтобы номер состоял ТОЛЬКО из цифр");
        System.out.println("   Это означает, что номера вроде '4222 2222 2222 2' (13 цифр с пробелами) не проходят");
        System.out.println("2. Первая часть требует РОВНО 16 цифр в формате 4 группы по 4");

        System.out.println("\nПредлагаемые улучшения:");
        System.out.println("1. Разрешить пробелы/дефисы в любом месте для чисел 13-19 цифр:");
        System.out.println("   \"^((\\d{4}[- ]?){3}\\d{4})|(\\d[- ]?){12,18}\\d$\"");
        System.out.println("2. Или нормализовать номера перед проверкой (удалить все нецифровые символы)");
        System.out.println("3. Или использовать более гибкий паттерн:");
        System.out.println("   \"^(\\d[- ]?){12,18}\\d$\" - от 13 до 19 цифр с произвольными разделителями");

        System.out.println("\nАльтернативный паттерн:");
        String improvedPattern = "^(\\d{4}[- ]?){3}\\d{4}$|^(\\d[- ]?){12,18}\\d$";
        System.out.println(improvedPattern);

        // Проверяем, работает ли улучшенный паттерн
        Pattern improved = Pattern.compile(improvedPattern);
        System.out.println("\nПроверка улучшенным паттерном:");

        for (String cardNumber : TestCardNumbers.testNumbers) {
            boolean original = PATTERN.matcher(cardNumber).matches();
            boolean improvedResult = improved.matcher(cardNumber).matches();

            if (original != improvedResult) {
                System.out.printf("%-35s: старый=%s, новый=%s %s%n",
                        cardNumber, original, improvedResult,
                        !original ? "✓ ИСПРАВЛЕНО" : "");
            }
        }
    }
}
