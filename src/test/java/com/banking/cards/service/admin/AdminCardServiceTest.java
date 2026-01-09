package com.banking.cards.service.admin;

import com.banking.cards.common.CardStatus;
import com.banking.cards.common.audit.AuditAction;
import com.banking.cards.common.audit.AuditEntityType;
import com.banking.cards.config.CardSettingsConfig;
import com.banking.cards.dto.request.AdminCreateCardRequest;
import com.banking.cards.dto.response.AdminCardDto;
import com.banking.cards.dto.response.PageResponse;
import com.banking.cards.entity.Card;
import com.banking.cards.entity.User;
import com.banking.cards.exceptions.BadBalanceException;
import com.banking.cards.mapper.CardMapper;
import com.banking.cards.repository.CardRepository;
import com.banking.cards.repository.UserRepository;
import com.banking.cards.service.AuditService;
import com.banking.cards.util.CardNumberGenerator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminCardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private CardSettingsConfig cardConfig;

    @Mock
    private CardNumberGenerator cardNumberGenerator;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AdminCardService adminCardService;

    private UUID testUserId;
    private User testUser;
    private Card testCard;
    private AdminCardDto testAdminCardDto;
    private AdminCreateCardRequest testCreateCardRequest;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        testUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        testUser = User.builder()
                .uniqueKey(testUserId)
                .username("testuser")
                .build();

        testCard = Card.builder()
                .id(1L)
                .cardNumber("4276550012345678")
                .owner(testUser)
                .balance(BigDecimal.ZERO)
                .validityPeriod(YearMonth.of(2029, 12))
                .status(CardStatus.ACTIVE)
                .build();

        testAdminCardDto = new AdminCardDto(
                "4276550012345678",
                YearMonth.of(2029, 12),
                CardStatus.ACTIVE,
                "100.0"
        );

        testCreateCardRequest = new AdminCreateCardRequest(
                testUserId,
                YearMonth.of(2029, 12),
                BigDecimal.ZERO
        );

        testPageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("getCard - Успешное создание карты без сохранения")
    void getCard_shouldReturnCard_whenUserExists() {
        // Arrange
        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardConfig.getPrefix()).thenReturn("427655");
        when(cardNumberGenerator.generateCardNumber("427655")).thenReturn("4276550012345678");

        // Act
        Card result = adminCardService.getCard(testCreateCardRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getOwner()).isEqualTo(testUser);
        assertThat(result.getCardNumber()).isEqualTo("4276550012345678");
        assertThat(result.getValidityPeriod()).isEqualTo(YearMonth.of(2029, 12));
        assertThat(result.getStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(result.getBalance()).isEqualTo(BigDecimal.ZERO);

        verify(userRepository).findByUniqueKey(testUserId);
        verify(cardConfig).getPrefix();
    }

    @Test
    @DisplayName("getCard - Бросает исключение при отсутствии пользователя")
    void getCard_shouldThrowException_whenUserNotFound() {
        // Arrange
        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> adminCardService.getCard(testCreateCardRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findByUniqueKey(testUserId);
        verifyNoInteractions(cardConfig);
    }

    @Test
    @DisplayName("createCard - Успешное создание и сохранение карты")
    void createCard_shouldSaveCardAndReturnDto() {
        // Arrange
        Card newCard = Card.builder()
                .cardNumber("4276550098765432")
                .owner(testUser)
                .validityPeriod(YearMonth.of(2029, 12))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();

        AdminCardDto expectedDto = new AdminCardDto(
                "4276550098765432",
                YearMonth.of(2029, 12),
                CardStatus.ACTIVE,
                "0.0"
        );

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardConfig.getPrefix()).thenReturn("427655");
        when(cardNumberGenerator.generateCardNumber("427655")).thenReturn("4276550098765432");
        when(cardRepository.save(any(Card.class))).thenReturn(newCard);
        when(cardMapper.toAdminDto(newCard)).thenReturn(expectedDto);

        // Act
        AdminCardDto result = adminCardService.createCard(testCreateCardRequest);

        // Assert
        assertThat(result).isEqualTo(expectedDto);

        verify(userRepository).findByUniqueKey(testUserId);
        verify(cardRepository).save(any(Card.class));
        verify(cardMapper).toAdminDto(newCard);
        verify(auditService).log(
                eq(AuditAction.CARD_CREATED),
                eq(AuditEntityType.CARD),
                eq("4276550098765432"),
                eq("ownerUser=" + testUserId)
        );
    }

    @Test
    @DisplayName("createCard - Создает карту с балансом")
    void createCard_shouldCreateCardWithBalance() {
        // Arrange
        AdminCreateCardRequest requestWithBalance = new AdminCreateCardRequest(
                testUserId,
                YearMonth.of(2030, 6),
                BigDecimal.valueOf(1000.50)
        );

        Card cardWithBalance = Card.builder()
                .cardNumber("4276550011112222")
                .owner(testUser)
                .validityPeriod(YearMonth.of(2030, 6))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000.50))
                .build();

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardConfig.getPrefix()).thenReturn("427655");
        when(cardNumberGenerator.generateCardNumber("427655")).thenReturn("4276550011112222");
        when(cardRepository.save(any(Card.class))).thenReturn(cardWithBalance);
        when(cardMapper.toAdminDto(cardWithBalance)).thenReturn(new AdminCardDto(
                "4276550011112222",
                YearMonth.of(2030, 6),
                CardStatus.ACTIVE,
                "1000.50"
        ));

        // Act
        AdminCardDto result = adminCardService.createCard(requestWithBalance);

        // Assert
        assertThat(result.balance()).isEqualTo("1000.50");
    }

    @Test
    @DisplayName("deleteCard - Успешное удаление карты с нулевым балансом")
    void deleteCard_shouldDeleteCard_whenBalanceIsZero() {
        // Arrange
        when(cardRepository.findByCardNumber("4276550012345678"))
                .thenReturn(Optional.of(testCard));

        // Act
        adminCardService.deleteCard("4276550012345678");

        // Assert
        verify(cardRepository).delete(testCard);
        verify(auditService).log(
                eq(AuditAction.CARD_DELETED),
                eq(AuditEntityType.CARD),
                eq("4276550012345678"),
                eq("balance=" + BigDecimal.ZERO)
        );
    }

    @Test
    @DisplayName("deleteCard - Бросает исключение при отсутствии карты")
    void deleteCard_shouldThrowException_whenCardNotFound() {
        // Arrange
        when(cardRepository.findByCardNumber("9999999999999999"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> adminCardService.deleteCard("9999999999999999"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Card not found");

        verify(cardRepository, never()).delete(any());
        verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("deleteCard - Бросает исключение при ненулевом балансе")
    void deleteCard_shouldThrowException_whenBalanceNotZero() {
        // Arrange
        Card cardWithBalance = Card.builder()
                .cardNumber("4276550012345678")
                .owner(testUser)
                .balance(BigDecimal.valueOf(100.50))
                .build();

        when(cardRepository.findByCardNumber("4276550012345678"))
                .thenReturn(Optional.of(cardWithBalance));

        // Act & Assert
        assertThatThrownBy(() -> adminCardService.deleteCard("4276550012345678"))
                .isInstanceOf(BadBalanceException.class)
                .hasMessage("Card balance should be zero");

        verify(cardRepository, never()).delete(any());
        verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("changeStatus - Успешное изменение статуса карты")
    void changeStatus_shouldChangeStatusAndLogAudit() {
        // Arrange
        Card cardToUpdate = Card.builder()
                .cardNumber("4276550012345678")
                .owner(testUser)
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findByCardNumber("4276550012345678"))
                .thenReturn(Optional.of(cardToUpdate));

        // Act
        adminCardService.changeStatus("4276550012345678", CardStatus.BLOCKED);

        // Assert
        assertThat(cardToUpdate.getStatus()).isEqualTo(CardStatus.BLOCKED);
        verify(cardRepository).findByCardNumber("4276550012345678");
        verify(auditService).log(
                eq(AuditAction.CARD_STATUS_CHANGED),
                eq(AuditEntityType.CARD),
                eq("4276550012345678"),
                eq("oldStatus=ACTIVE;newStatus=BLOCKED;cardNumber= 4276550012345678")
        );
    }

    @Test
    @DisplayName("changeStatus - Не изменяет статус при совпадении значений")
    void changeStatus_shouldNotChange_whenStatusIsSame() {
        // Arrange
        Card cardToUpdate = Card.builder()
                .cardNumber("4276550012345678")
                .owner(testUser)
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findByCardNumber("4276550012345678"))
                .thenReturn(Optional.of(cardToUpdate));

        // Act
        adminCardService.changeStatus("4276550012345678", CardStatus.ACTIVE);

        // Assert
        assertThat(cardToUpdate.getStatus()).isEqualTo(CardStatus.ACTIVE);
        verify(cardRepository).findByCardNumber("4276550012345678");
        verify(auditService, never()).log(any(), any(), any(), any());
    }

    @Test
    @DisplayName("changeStatus - Бросает исключение при отсутствии карты")
    void changeStatus_shouldThrowException_whenCardNotFound() {
        // Arrange
        when(cardRepository.findByCardNumber("9999999999999999"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> adminCardService.changeStatus("9999999999999999", CardStatus.BLOCKED))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Card not found");

        verify(cardRepository).findByCardNumber("9999999999999999");
        verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("getUserCards - Успешное получение карт пользователя")
    void getUserCards_shouldReturnCards_whenUserExists() {
        // Arrange
        Page<Card> cardPage = new PageImpl<>(List.of(testCard), testPageable, 1);

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findAllByOwner(testUser, testPageable))
                .thenReturn(cardPage);
        when(cardMapper.toAdminDto(testCard)).thenReturn(testAdminCardDto);

        // Act
        PageResponse<AdminCardDto> result = adminCardService.getUserCards(testUserId, testPageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.content().size()).isEqualTo(1);
        assertThat(result.content().get(0)).isEqualTo(testAdminCardDto);
        assertThat(result.totalElements()).isEqualTo(1);

        verify(userRepository).findByUniqueKey(testUserId);
        verify(cardRepository).findAllByOwner(testUser, testPageable);
        verify(cardMapper).toAdminDto(testCard);
    }

    @Test
    @DisplayName("getUserCards - Бросает исключение при отсутствии пользователя")
    void getUserCards_shouldThrowException_whenUserNotFound() {
        // Arrange
        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> adminCardService.getUserCards(testUserId, testPageable))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findByUniqueKey(testUserId);
        verifyNoInteractions(cardRepository, cardMapper);
    }

    @Test
    @DisplayName("getUserCards - Возвращает пустую страницу при отсутствии карт")
    void getUserCards_shouldReturnEmptyPage_whenUserHasNoCards() {
        // Arrange
        Page<Card> emptyPage = new PageImpl<>(List.of(), testPageable, 0);

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findAllByOwner(testUser, testPageable))
                .thenReturn(emptyPage);

        // Act
        PageResponse<AdminCardDto> result = adminCardService.getUserCards(testUserId, testPageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.content().isEmpty());
        assertThat(result.totalElements()).isEqualTo(0);

        verify(userRepository).findByUniqueKey(testUserId);
        verify(cardRepository).findAllByOwner(testUser, testPageable);
        verifyNoInteractions(cardMapper);
    }

    @Test
    @DisplayName("getUserCards - Корректно обрабатывает пагинацию")
    void getUserCards_shouldHandlePaginationCorrectly() {
        // Arrange
        Pageable customPageable = PageRequest.of(2, 5);
        Page<Card> cardPage = new PageImpl<>(List.of(testCard), customPageable, 25);

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findAllByOwner(testUser, customPageable))
                .thenReturn(cardPage);
        when(cardMapper.toAdminDto(testCard)).thenReturn(testAdminCardDto);

        // Act
        PageResponse<AdminCardDto> result = adminCardService.getUserCards(testUserId, customPageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.page()).isEqualTo(2);
        assertThat(result.totalPages()).isEqualTo(5);
        assertThat(result.totalElements()).isEqualTo(25);

        verify(cardRepository).findAllByOwner(testUser, customPageable);
    }
}