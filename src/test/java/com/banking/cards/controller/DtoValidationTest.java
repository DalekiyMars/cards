package com.banking.cards.controller;

import com.banking.cards.constants.TestCardNumbers;
import com.banking.cards.dto.request.AdminCreateCardRequest;
import com.banking.cards.dto.request.CardAmountRequest;
import com.banking.cards.dto.request.CardNumberAndAmountRequest;
import com.banking.cards.dto.request.CardNumberRequest;
import com.banking.cards.dto.request.CardTransferRequest;
import com.banking.cards.dto.request.ChangeUserRoleRequest;
import com.banking.cards.dto.request.LoginRequest;
import com.banking.cards.dto.request.SideServiceRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static com.banking.cards.constants.TestConstants.CARD_PATTERN;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Комплексный тестовый класс для проверки валидации всех DTO в системе.
 * Использует вложенные классы для организации тестов по типам DTO.
 */
class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private static String normalizeCardNumber(String cardNumber) {
        return cardNumber == null ? null : cardNumber.replaceAll("[^0-9]", "");
    }

    /**
     * Вспомогательный класс для организации тестов карточных номеров.
     */
    @Nested
    @DisplayName("Test Card Numbers Integration Tests")
    class TestCardNumbersIntegrationTest {

        @Test
        @DisplayName("Анализ соответствия всех тестовых номеров паттерну CARD_PATTERN")
        void analyzeAllTestCardsAgainstPattern() {
            System.out.println("\n=== АНАЛИЗ СООТВЕТСТВИЯ ТЕСТОВЫХ НОМЕРОВ ПАТТЕРНУ ===");
            System.out.println("Паттерн: " + CARD_PATTERN);

            List<String> patternMatchingCards = new ArrayList<>();
            List<String> nonPatternMatchingCards = new ArrayList<>();

            for (String cardNumber : TestCardNumbers.testNumbers) {
                if (cardNumber.matches(CARD_PATTERN)) {
                    patternMatchingCards.add(cardNumber);
                } else {
                    nonPatternMatchingCards.add(cardNumber);
                }
            }

            System.out.println("\nСоответствуют паттерну (" + patternMatchingCards.size() + "):");
            patternMatchingCards.forEach(card -> System.out.println("  ✓ " + card));

            System.out.println("\nНЕ соответствуют паттерну (" + nonPatternMatchingCards.size() + "):");
            nonPatternMatchingCards.forEach(card -> System.out.println("  ✗ " + card + " (только цифры: " + normalizeCardNumber(card) +
                    ", длина: " + normalizeCardNumber(card).length() + ")"));

            // Для тестов DTO нам нужно использовать только карты, соответствующие паттерну
            assertThat(patternMatchingCards)
                    .as("Должна быть хотя бы одна карта, соответствующая паттерну")
                    .isNotEmpty();
        }

        @ParameterizedTest
        @MethodSource("provideValidCardNumbersForDTO")
        @DisplayName("Валидные номера карт (по паттерну) должны проходить валидацию в DTO")
        void validCardNumbersByPattern_shouldPassDTOValidation(String cardNumber, String description) {
            // Проверяем CardNumberAndAmountRequest
            CardNumberAndAmountRequest request1 = new CardNumberAndAmountRequest(
                    cardNumber,
                    BigDecimal.valueOf(100.00)
            );

            Set<ConstraintViolation<CardNumberAndAmountRequest>> violations1 = validator.validate(request1);

            assertThat(violations1)
                    .as("Карта '%s' (%s) должна проходить валидацию в CardNumberAndAmountRequest",
                            cardNumber, description)
                    .isEmpty();

            // Проверяем CardNumberRequest
            CardNumberRequest request2 = new CardNumberRequest();
            request2.setCardNumber(cardNumber);

            Set<ConstraintViolation<CardNumberRequest>> violations2 = validator.validate(request2);

            assertThat(violations2)
                    .as("Карта '%s' (%s) должна проходить валидацию в CardNumberRequest",
                            cardNumber, description)
                    .isEmpty();
        }

        @ParameterizedTest
        @MethodSource("provideValidCardNumberPairsForTransfer")
        @DisplayName("Валидные пары номеров карт (по паттерну) должны работать в CardTransferRequest")
        void validCardNumberPairs_shouldWorkInTransferRequest(String fromCard, String toCard) {
            CardTransferRequest request = new CardTransferRequest(
                    fromCard,
                    toCard,
                    BigDecimal.valueOf(500.00)
            );

            Set<ConstraintViolation<CardTransferRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .as("Перевод с '%s' на '%s' должен проходить валидацию", fromCard, toCard)
                    .isEmpty();
        }

        @Test
        @DisplayName("Проверка нормализации номеров карт")
        void cardNumberNormalization_shouldWorkCorrectly() {
            Map<String, String> testCases = Map.of(
                    "4111 1111 1111 1111", "4111111111111111",
                    "4111-1111-1111-1111", "4111111111111111",
                    "4111 1111-1111 1111", "4111111111111111",
                    "4222 2222 2222 2", "4222222222222",
                    "3782 8224 6310 005", "378282246310005"
            );

            testCases.forEach((input, expected) -> {
                String normalized = normalizeCardNumber(input);

                assertThat(normalized)
                        .as("Номер '%s' должен нормализоваться в '%s'", input, expected)
                        .isEqualTo(expected);

                // Проверяем, что нормализованный номер соответствует второй части паттерна
                boolean matchesSecondPart = normalized.matches("^\\d{13,19}$");
                assertThat(matchesSecondPart)
                        .as("Нормализованный номер '%s' должен соответствовать ^\\d{13,19}$", normalized)
                        .isTrue();
            });
        }

        private static Stream<Arguments> provideValidCardNumbersForDTO() {
            return Arrays.stream(TestCardNumbers.testNumbers)
                    .filter(a -> a.matches(CARD_PATTERN))
                    .map(card -> Arguments.of(card, getCardDescription(card)));
        }

        private static Stream<Arguments> provideValidCardNumberPairsForTransfer() {
            // Фильтруем только карты, соответствующие паттерну
            List<String> validCards = Arrays.stream(TestCardNumbers.testNumbers)
                    .filter( a -> a.matches(CARD_PATTERN))
                    .toList();

            List<Arguments> arguments = new ArrayList<>();

            // Создаем пары различных ВАЛИДНЫХ карт для тестирования переводов
            for (int i = 0; i < validCards.size() - 1; i++) {
                for (int j = i + 1; j < validCards.size(); j++) {
                    arguments.add(Arguments.of(validCards.get(i), validCards.get(j)));
                }
            }

            // Если мало валидных пар, создаем хотя бы одну
            if (arguments.isEmpty() && validCards.size() >= 2) {
                arguments.add(Arguments.of(validCards.get(0), validCards.get(1)));
            }

            return arguments.stream().limit(5);
        }

        private static String getCardDescription(String cardNumber) {
            String digitsOnly = normalizeCardNumber(cardNumber);
            int length = digitsOnly.length();

            if (cardNumber.contains(" ")) {
                return length + " цифр с пробелами";
            } else if (cardNumber.contains("-")) {
                return length + " цифр с дефисами";
            } else {
                return length + " цифр без разделителей";
            }
        }
    }

    @Nested
    @DisplayName("AdminCreateCardRequest Validation Tests")
    class AdminCreateCardRequestValidation {

        @Test
        @DisplayName("Валидный запрос должен проходить валидацию")
        void validRequest_shouldPassValidation() {
            AdminCreateCardRequest request = new AdminCreateCardRequest(
                    UUID.randomUUID(),
                    YearMonth.now().plusMonths(1),
                    BigDecimal.valueOf(100.50)
            );

            Set<ConstraintViolation<AdminCreateCardRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Нулевой userId должен вызывать ошибку валидации")
        void nullUserId_shouldFailValidation() {
            AdminCreateCardRequest request = new AdminCreateCardRequest(
                    null,
                    YearMonth.now().plusMonths(1),
                    BigDecimal.TEN
            );

            Set<ConstraintViolation<AdminCreateCardRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("must be not null"));
        }

        @Test
        @DisplayName("Прошедшая дата действия должна вызывать ошибку")
        void pastValidityPeriod_shouldFailValidation() {
            AdminCreateCardRequest request = new AdminCreateCardRequest(
                    UUID.randomUUID(),
                    YearMonth.now().minusMonths(1),
                    BigDecimal.TEN
            );

            Set<ConstraintViolation<AdminCreateCardRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessageTemplate)
                    .anyMatch(template -> template.contains("must be a future date"));
        }

        @Test
        @DisplayName("Отрицательный баланс должен вызывать ошибку")
        void negativeBalance_shouldFailValidation() {
            AdminCreateCardRequest request = new AdminCreateCardRequest(
                    UUID.randomUUID(),
                    YearMonth.now().plusMonths(1),
                    BigDecimal.valueOf(-50.00)
            );

            Set<ConstraintViolation<AdminCreateCardRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("must be greater than zero"));
        }

        @Test
        @DisplayName("Баланс с более чем 2 знаками после запятой должен вызывать ошибку")
        void balanceWithTooManyDecimals_shouldFailValidation() {
            AdminCreateCardRequest request = new AdminCreateCardRequest(
                    UUID.randomUUID(),
                    YearMonth.now().plusMonths(1),
                    new BigDecimal("100.123")
            );

            Set<ConstraintViolation<AdminCreateCardRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessageTemplate)
                    .anyMatch(template -> template.contains("numeric value out of bounds"));
        }
    }

    @Nested
    @DisplayName("CardAmountRequest Validation Tests")
    class CardAmountRequestValidation {

        @Test
        @DisplayName("Валидная сумма должна проходить валидацию")
        void validAmount_shouldPassValidation() {
            CardAmountRequest request = new CardAmountRequest(BigDecimal.valueOf(50.75));

            Set<ConstraintViolation<CardAmountRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Нулевая сумма должна вызывать ошибку")
        void zeroAmount_shouldFailValidation() {
            CardAmountRequest request = new CardAmountRequest(BigDecimal.ZERO);

            Set<ConstraintViolation<CardAmountRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("must be greater than zero"));
        }

        @Test
        @DisplayName("Отрицательная сумма должна вызывать ошибку")
        void negativeAmount_shouldFailValidation() {
            CardAmountRequest request = new CardAmountRequest(BigDecimal.valueOf(-10.00));

            Set<ConstraintViolation<CardAmountRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("must be greater than zero"));
        }

        @Test
        @DisplayName("Нулевая сумма (null) должна вызывать ошибку")
        void nullAmount_shouldFailValidation() {
            CardAmountRequest request = new CardAmountRequest(null);

            Set<ConstraintViolation<CardAmountRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("is required"));
        }
    }

    @Nested
    @DisplayName("CardNumberAndAmountRequest Validation Tests")
    class CardNumberAndAmountRequestValidation {

        private static Stream<Arguments> invalidCardNumbers() {
            return Stream.of(
                    Arguments.of("", "пустая строка"),
                    Arguments.of("   ", "только пробелы"),
                    Arguments.of("1234", "слишком короткий номер"),
                    Arguments.of("12345678901234567890", "слишком длинный номер"),
                    Arguments.of("ABCD-EFGH-IJKL-MNOP", "не цифровые символы")
            );
        }

        @Test
        @DisplayName("Валидный запрос должен проходить валидацию")
        void validRequest_shouldPassValidation() {
            CardNumberAndAmountRequest request = new CardNumberAndAmountRequest(
                    "4111111111111111",
                    BigDecimal.valueOf(100.00)
            );

            Set<ConstraintViolation<CardNumberAndAmountRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @MethodSource("invalidCardNumbers")
        @DisplayName("Невалидные номера карт должны вызывать ошибку")
        void invalidCardNumbers_shouldFailValidation(String cardNumber, String description) {
            CardNumberAndAmountRequest request = new CardNumberAndAmountRequest(
                    cardNumber,
                    BigDecimal.valueOf(50.00)
            );

            Set<ConstraintViolation<CardNumberAndAmountRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .as("Невалидный номер карты: %s", description)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("Wrong card number"));
        }

        @Test
        @DisplayName("Нулевая сумма с валидным номером карты должна вызывать ошибку")
        void zeroAmountWithValidCard_shouldFailValidation() {
            CardNumberAndAmountRequest request = new CardNumberAndAmountRequest(
                    "4111111111111111",
                    BigDecimal.ZERO
            );

            Set<ConstraintViolation<CardNumberAndAmountRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("must be greater than zero"));
        }
    }

    @Nested
    @DisplayName("CardNumberRequest Validation Tests")
    class CardNumberRequestValidation {

        @Test
        @DisplayName("Валидный номер карты должен проходить валидацию")
        void validCardNumber_shouldPassValidation() {
            CardNumberRequest request = new CardNumberRequest();
            request.setCardNumber("5555555555554444");

            Set<ConstraintViolation<CardNumberRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Номер карты с пробелами и дефисами должен нормализоваться")
        void formattedCardNumber_shouldBeNormalized() {
            CardNumberRequest request = new CardNumberRequest();
            request.setCardNumber("5555-5555-5555-4444");

            String normalized = request.getCardNumber();

            assertThat(normalized).isEqualTo("5555555555554444");
        }

        @Test
        @DisplayName("Пустой номер карты должен вызывать ошибку")
        void emptyCardNumber_shouldFailValidation() {
            CardNumberRequest request = new CardNumberRequest();
            request.setCardNumber("");

            Set<ConstraintViolation<CardNumberRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("Card number required"));
        }
    }

    @Nested
    @DisplayName("CardTransferRequest Validation Tests")
    class CardTransferRequestValidation {

        @Test
        @DisplayName("Валидный запрос на перевод должен проходить валидацию")
        void validTransferRequest_shouldPassValidation() {
            CardTransferRequest request = new CardTransferRequest(
                    "5555555555554444",
                    "4111111111111111",
                    BigDecimal.valueOf(500.00)
            );

            Set<ConstraintViolation<CardTransferRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Нулевая сумма перевода должна вызывать ошибку")
        void zeroAmountTransfer_shouldFailValidation() {
            CardTransferRequest request = new CardTransferRequest(
                    "5555555555554444",
                    "4111111111111111",
                    BigDecimal.ZERO
            );

            Set<ConstraintViolation<CardTransferRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("must be greater than zero"));
        }
    }

    @Nested
    @DisplayName("LoginRequest Validation Tests")
    class LoginRequestValidation {

        @Test
        @DisplayName("Валидные учетные данные должны проходить валидацию")
        void validCredentials_shouldPassValidation() {
            LoginRequest request = new LoginRequest("username", "password123");

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Слишком короткий пароль должен вызывать ошибку")
        void shortPassword_shouldFailValidation() {
            LoginRequest request = new LoginRequest("username", "123");

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("must be at least 6 characters"));
        }

        @Test
        @DisplayName("Пустое имя пользователя должно вызывать ошибку")
        void emptyUsername_shouldFailValidation() {
            LoginRequest request = new LoginRequest("", "password123");

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("is required"));
        }
    }

    @Nested
    @DisplayName("ChangeUserRoleRequest Validation Tests")
    class ChangeUserRoleRequestValidation {

        @Test
        @DisplayName("Валидный запрос на изменение роли должен проходить валидацию")
        void validRoleChange_shouldPassValidation() {
            ChangeUserRoleRequest request = new ChangeUserRoleRequest(
                    UUID.randomUUID(),
                    com.banking.cards.common.Role.ADMIN
            );

            Set<ConstraintViolation<ChangeUserRoleRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Нулевой UUID должен вызывать ошибку")
        void nullUserId_shouldFailValidation() {
            ChangeUserRoleRequest request = new ChangeUserRoleRequest(
                    null,
                    com.banking.cards.common.Role.USER
            );

            Set<ConstraintViolation<ChangeUserRoleRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("must be not null"));
        }

        @Test
        @DisplayName("Нулевая роль должна вызывать ошибку")
        void nullRole_shouldFailValidation() {
            ChangeUserRoleRequest request = new ChangeUserRoleRequest(
                    UUID.randomUUID(),
                    null
            );

            Set<ConstraintViolation<ChangeUserRoleRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("must be not null"));
        }
    }

    @Nested
    @DisplayName("SideServiceRequest Validation Tests")
    class SideServiceRequestValidation {

        @Test
        @DisplayName("Валидный запрос сервиса должен проходить валидацию")
        void validServiceRequest_shouldPassValidation() {
            SideServiceRequest request = new SideServiceRequest(
                    "billing-service",
                    "b1f8c9d2-4a7e-4f12-a89d-123456789abc"
            );

            Set<ConstraintViolation<SideServiceRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Пустое имя сервиса должно вызывать ошибку")
        void emptyServiceName_shouldFailValidation() {
            SideServiceRequest request = new SideServiceRequest(
                    "",
                    "valid-api-key"
            );

            Set<ConstraintViolation<SideServiceRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("must not be blank"));
        }

        @Test
        @DisplayName("Пустой API ключ должен вызывать ошибку")
        void emptyApiKey_shouldFailValidation() {
            SideServiceRequest request = new SideServiceRequest(
                    "valid-service",
                    ""
            );

            Set<ConstraintViolation<SideServiceRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("must not be blank"));
        }
    }
}