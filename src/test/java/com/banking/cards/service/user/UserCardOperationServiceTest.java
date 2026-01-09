package com.banking.cards.service.user;

import com.banking.cards.common.CardStatus;
import com.banking.cards.common.audit.AuditAction;
import com.banking.cards.common.audit.AuditEntityType;
import com.banking.cards.entity.Card;
import com.banking.cards.entity.User;
import com.banking.cards.repository.CardRepository;
import com.banking.cards.repository.UserRepository;
import com.banking.cards.service.AuditService;
import com.banking.cards.service.CardOperationService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCardOperationServiceTest {
    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardOperationService cardOperationService;

    @Mock
    private AuditService auditService;

    @Captor
    private ArgumentCaptor<String> auditDetailsCaptor;

    @InjectMocks
    private UserCardOperationService userCardOperationService;

    private UUID testUserId;
    private User testUser;
    private Card testCard1;
    private Card testCard2;

    @BeforeEach
    void setUp() {
        testUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        testUser = User.builder()
                .uniqueKey(testUserId)
                .username("testuser")
                .build();

        testCard1 = Card.builder()
                .id(1L)
                .cardNumber("4276550012345678")
                .owner(testUser)
                .balance(new BigDecimal("1000.00"))
                .validityPeriod(YearMonth.of(2029, 12))
                .status(CardStatus.ACTIVE)
                .build();

        testCard2 = Card.builder()
                .id(2L)
                .cardNumber("4276550098765432")
                .owner(testUser)
                .balance(new BigDecimal("500.00"))
                .validityPeriod(YearMonth.of(2029, 12))
                .status(CardStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("transfer - Успешный перевод между картами пользователя")
    void transfer_shouldTransferAmountBetweenUserCards() {
        // Arrange
        BigDecimal amount = new BigDecimal("200.00");

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndOwner("4276550012345678", testUser))
                .thenReturn(Optional.of(testCard1));
        when(cardRepository.findByCardNumberAndOwner("4276550098765432", testUser))
                .thenReturn(Optional.of(testCard2));

        // Act
        userCardOperationService.transfer("4276550012345678", "4276550098765432", amount, testUserId);

        // Assert
        // Проверяем изменение балансов
        assertThat(testCard1.getBalance()).isEqualTo(new BigDecimal("800.00"));
        assertThat(testCard2.getBalance()).isEqualTo(new BigDecimal("700.00"));

        // Проверяем вызовы сервиса операций
        verify(cardOperationService).logTransfer(testCard1, testCard2, amount);

        // Проверяем вызовы аудита
        verify(auditService).log(
                eq(AuditAction.CARD_TRANSFER_OUT),
                eq(AuditEntityType.CARD),
                eq("4276550012345678"),
                eq("to=4276550098765432;amount=200.00")
        );

        verify(auditService).log(
                eq(AuditAction.CARD_TRANSFER_IN),
                eq(AuditEntityType.CARD),
                eq("4276550098765432"),
                eq("from=4276550012345678;amount=200.00")
        );
    }

    @Test
    @DisplayName("transfer - Бросает исключение при недостатке средств")
    void transfer_shouldThrowException_whenInsufficientFunds() {
        // Arrange
        BigDecimal amount = new BigDecimal("1500.00"); // Больше, чем баланс карты 1

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndOwner("4276550012345678", testUser))
                .thenReturn(Optional.of(testCard1));
        when(cardRepository.findByCardNumberAndOwner("4276550098765432", testUser))
                .thenReturn(Optional.of(testCard2));

        // Act & Assert
        assertThatThrownBy(() ->
                userCardOperationService.transfer("4276550012345678", "4276550098765432", amount, testUserId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Insufficient funds");

        // Проверяем, что балансы не изменились
        assertThat(testCard1.getBalance()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(testCard2.getBalance()).isEqualTo(new BigDecimal("500.00"));

        verifyNoInteractions(cardOperationService, auditService);
    }

    @Test
    @DisplayName("transfer - Бросает исключение, если карта отправителя неактивна")
    void transfer_shouldThrowException_whenFromCardIsNotActive() {
        // Arrange
        BigDecimal amount = new BigDecimal("200.00");
        testCard1.setStatus(CardStatus.BLOCKED);

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndOwner("4276550012345678", testUser))
                .thenReturn(Optional.of(testCard1));
        when(cardRepository.findByCardNumberAndOwner("4276550098765432", testUser))
                .thenReturn(Optional.of(testCard2));

        // Act & Assert
        assertThatThrownBy(() ->
                userCardOperationService.transfer("4276550012345678", "4276550098765432", amount, testUserId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Card is not active");

        verifyNoInteractions(cardOperationService, auditService);
    }

    @Test
    @DisplayName("transfer - Бросает исключение, если карта получателя неактивна")
    void transfer_shouldThrowException_whenToCardIsNotActive() {
        // Arrange
        BigDecimal amount = new BigDecimal("200.00");
        testCard2.setStatus(CardStatus.BLOCKED);

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndOwner("4276550012345678", testUser))
                .thenReturn(Optional.of(testCard1));
        when(cardRepository.findByCardNumberAndOwner("4276550098765432", testUser))
                .thenReturn(Optional.of(testCard2));

        // Act & Assert
        assertThatThrownBy(() ->
                userCardOperationService.transfer("4276550012345678", "4276550098765432", amount, testUserId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Card is not active");

        verifyNoInteractions(cardOperationService, auditService);
    }

    @Test
    @DisplayName("transfer - Бросает исключение при отсутствии пользователя")
    void transfer_shouldThrowException_whenUserNotFound() {
        // Arrange
        BigDecimal amount = new BigDecimal("200.00");

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                userCardOperationService.transfer("4276550012345678", "4276550098765432", amount, testUserId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");

        verifyNoInteractions(cardRepository, cardOperationService, auditService);
    }

    @Test
    @DisplayName("transfer - Бросает исключение, если карта отправителя не найдена у пользователя")
    void transfer_shouldThrowException_whenFromCardNotFound() {
        // Arrange
        BigDecimal amount = new BigDecimal("200.00");

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndOwner("4276550012345678", testUser))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                userCardOperationService.transfer("4276550012345678", "4276550098765432", amount, testUserId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Card not found");

        verify(cardRepository, never()).findByCardNumberAndOwner("4276550098765432", testUser);
        verifyNoInteractions(cardOperationService, auditService);
    }

    @Test
    @DisplayName("transfer - Бросает исключение, если карта получателя не найдена у пользователя")
    void transfer_shouldThrowException_whenToCardNotFound() {
        // Arrange
        BigDecimal amount = new BigDecimal("200.00");

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndOwner("4276550012345678", testUser))
                .thenReturn(Optional.of(testCard1));
        when(cardRepository.findByCardNumberAndOwner("4276550098765432", testUser))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                userCardOperationService.transfer("4276550012345678", "4276550098765432", amount, testUserId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Card not found");

        verifyNoInteractions(cardOperationService, auditService);
    }

    @Test
    @DisplayName("deposit - Успешное пополнение карты")
    void deposit_shouldIncreaseCardBalance() {
        // Arrange
        BigDecimal amount = new BigDecimal("300.00");

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndOwner("4276550012345678", testUser))
                .thenReturn(Optional.of(testCard1));

        // Act
        userCardOperationService.deposit("4276550012345678", amount, testUserId);

        // Assert
        assertThat(testCard1.getBalance()).isEqualTo(new BigDecimal("1300.00"));

        verify(cardOperationService).logDeposit(testCard1, amount);
        verify(auditService).log(
                eq(AuditAction.CARD_DEPOSIT),
                eq(AuditEntityType.CARD),
                eq("4276550012345678"),
                eq("amount=300.00")
        );
    }

    @Test
    @DisplayName("deposit - Бросает исключение, если карта неактивна")
    void deposit_shouldThrowException_whenCardIsNotActive() {
        // Arrange
        BigDecimal amount = new BigDecimal("300.00");
        testCard1.setStatus(CardStatus.BLOCKED);

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndOwner("4276550012345678", testUser))
                .thenReturn(Optional.of(testCard1));

        // Act & Assert
        assertThatThrownBy(() ->
                userCardOperationService.deposit("4276550012345678", amount, testUserId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Card is not active");

        verifyNoInteractions(cardOperationService, auditService);
    }

    @Test
    @DisplayName("deposit - Бросает исключение при отсутствии пользователя")
    void deposit_shouldThrowException_whenUserNotFound() {
        // Arrange
        BigDecimal amount = new BigDecimal("300.00");

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                userCardOperationService.deposit("4276550012345678", amount, testUserId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");

        verifyNoInteractions(cardRepository, cardOperationService, auditService);
    }

    @Test
    @DisplayName("deposit - Бросает исключение, если карта не найдена у пользователя")
    void deposit_shouldThrowException_whenCardNotFound() {
        // Arrange
        BigDecimal amount = new BigDecimal("300.00");

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndOwner("4276550012345678", testUser))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                userCardOperationService.deposit("4276550012345678", amount, testUserId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Card not found");

        verifyNoInteractions(cardOperationService, auditService);
    }

    @Test
    @DisplayName("withdraw - Успешное снятие средств с карты")
    void withdraw_shouldDecreaseCardBalance() {
        // Arrange
        BigDecimal amount = new BigDecimal("200.00");

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndOwner("4276550012345678", testUser))
                .thenReturn(Optional.of(testCard1));

        // Act
        userCardOperationService.withdraw("4276550012345678", amount, testUserId);

        // Assert
        assertThat(testCard1.getBalance()).isEqualTo(new BigDecimal("800.00"));

        verify(cardOperationService).logWithdraw(testCard1, amount);
        verify(auditService).log(
                eq(AuditAction.CARD_WITHDRAW),
                eq(AuditEntityType.CARD),
                eq("4276550012345678"),
                eq("amount=200.00;balanceBefore=1000.00")
        );
    }

    @Test
    @DisplayName("withdraw - Бросает исключение при недостатке средств")
    void withdraw_shouldThrowException_whenInsufficientFunds() {
        // Arrange
        BigDecimal amount = new BigDecimal("1500.00"); // Больше, чем баланс

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndOwner("4276550012345678", testUser))
                .thenReturn(Optional.of(testCard1));

        // Act & Assert
        assertThatThrownBy(() ->
                userCardOperationService.withdraw("4276550012345678", amount, testUserId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Insufficient funds");

        // Проверяем, что баланс не изменился
        assertThat(testCard1.getBalance()).isEqualTo(new BigDecimal("1000.00"));

        verifyNoInteractions(cardOperationService, auditService);
    }

    @Test
    @DisplayName("withdraw - Бросает исключение, если карта неактивна")
    void withdraw_shouldThrowException_whenCardIsNotActive() {
        // Arrange
        BigDecimal amount = new BigDecimal("200.00");
        testCard1.setStatus(CardStatus.BLOCKED);

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndOwner("4276550012345678", testUser))
                .thenReturn(Optional.of(testCard1));

        // Act & Assert
        assertThatThrownBy(() ->
                userCardOperationService.withdraw("4276550012345678", amount, testUserId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Card is not active");

        verifyNoInteractions(cardOperationService, auditService);
    }

    @Test
    @DisplayName("withdraw - Бросает исключение при отсутствии пользователя")
    void withdraw_shouldThrowException_whenUserNotFound() {
        // Arrange
        BigDecimal amount = new BigDecimal("200.00");

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                userCardOperationService.withdraw("4276550012345678", amount, testUserId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");

        verifyNoInteractions(cardRepository, cardOperationService, auditService);
    }

    @Test
    @DisplayName("withdraw - Бросает исключение, если карта не найдена у пользователя")
    void withdraw_shouldThrowException_whenCardNotFound() {
        // Arrange
        BigDecimal amount = new BigDecimal("200.00");

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndOwner("4276550012345678", testUser))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                userCardOperationService.withdraw("4276550012345678", amount, testUserId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Card not found");

        verifyNoInteractions(cardOperationService, auditService);
    }

    @Test
    @DisplayName("withdraw - Аудит включает баланс до снятия")
    void withdraw_shouldIncludeBalanceBeforeInAudit() {
        // Arrange
        BigDecimal amount = new BigDecimal("200.00");

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndOwner("4276550012345678", testUser))
                .thenReturn(Optional.of(testCard1));

        // Act
        userCardOperationService.withdraw("4276550012345678", amount, testUserId);

        // Assert
        verify(auditService).log(
                eq(AuditAction.CARD_WITHDRAW),
                eq(AuditEntityType.CARD),
                eq("4276550012345678"),
                auditDetailsCaptor.capture()
        );

        String auditDetails = auditDetailsCaptor.getValue();
        assertThat(auditDetails).contains("amount=200.00");
        assertThat(auditDetails).contains("balanceBefore=1000.00");
    }

    @Test
    @DisplayName("transfer - Проверка работы с разными статусами карт")
    void transfer_shouldHandleDifferentCardStatuses() {
        // Arrange
        BigDecimal amount = new BigDecimal("200.00");

        // Тестируем все статусы, кроме ACTIVE
        for (CardStatus status : CardStatus.values()) {
            if (status == CardStatus.ACTIVE) continue;

            testCard1.setStatus(status);

            when(userRepository.findByUniqueKey(testUserId))
                    .thenReturn(Optional.of(testUser));
            when(cardRepository.findByCardNumberAndOwner("4276550012345678", testUser))
                    .thenReturn(Optional.of(testCard1));
            when(cardRepository.findByCardNumberAndOwner("4276550098765432", testUser))
                    .thenReturn(Optional.of(testCard2));

            // Act & Assert
            assertThatThrownBy(() ->
                    userCardOperationService.transfer("4276550012345678", "4276550098765432", amount, testUserId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Card is not active");

            // Сбрасываем моки для следующей итерации
            reset(userRepository, cardRepository);
        }
    }

    @Test
    @DisplayName("getUserCard - Возвращает карту пользователя")
    void getUserCard_shouldReturnCard_whenCardBelongsToUser() {
        // Arrange
        String cardId = "4276550012345678";

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndOwner(cardId, testUser))
                .thenReturn(Optional.of(testCard1));

        // Act (используем рефлексию для вызова приватного метода или тестируем через публичные методы)
        // Для простоты тестируем через deposit, который использует getUserCard
        userCardOperationService.deposit(cardId, BigDecimal.TEN, testUserId);

        // Assert
        verify(cardRepository).findByCardNumberAndOwner(cardId, testUser);
    }

    @Test
    @DisplayName("validateCardIsActive - Бросает исключение для неактивной карты")
    void validateCardIsActive_shouldThrowExceptionForInactiveCard() {
        // Arrange
        testCard1.setStatus(CardStatus.BLOCKED);

        // Act & Assert через публичный метод
        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndOwner("4276550012345678", testUser))
                .thenReturn(Optional.of(testCard1));

        assertThatThrownBy(() ->
                userCardOperationService.deposit("4276550012345678", BigDecimal.TEN, testUserId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Card is not active");
    }
}