package com.banking.cards.service.api;

import com.banking.cards.common.MaskedBalanceValue;
import com.banking.cards.common.MaskedCardNumber;
import com.banking.cards.common.audit.AuditAction;
import com.banking.cards.common.audit.AuditEntityType;
import com.banking.cards.config.MaskingConfig;
import com.banking.cards.dto.request.AdminCreateCardRequest;
import com.banking.cards.dto.response.CardDto;
import com.banking.cards.dto.response.PageResponse;
import com.banking.cards.entity.Card;
import com.banking.cards.entity.User;
import com.banking.cards.mapper.CardMapper;
import com.banking.cards.repository.CardRepository;
import com.banking.cards.repository.UserRepository;
import com.banking.cards.service.AuditService;
import com.banking.cards.service.admin.AdminCardService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Import(MaskingConfig.class)
class ApiCardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private MaskingConfig maskingConfig;

    @Mock
    private AuditService auditService;

    @Mock
    private AdminCardService adminCardService;

    @InjectMocks
    private ApiCardService apiCardService;

    private UUID testUserId;
    private User testUser;
    private Card testCard;
    private CardDto testCardDto;
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
                .status(com.banking.cards.common.CardStatus.ACTIVE)
                .build();

        testCardDto = new CardDto(
                new MaskedCardNumber(maskingConfig, "4276550012345678"),
                YearMonth.of(2029, 12),
                com.banking.cards.common.CardStatus.ACTIVE,
                new MaskedBalanceValue(maskingConfig, BigDecimal.ZERO)
        );

        testCreateCardRequest = new AdminCreateCardRequest(
                testUserId,
                YearMonth.of(2029, 12),
                BigDecimal.ZERO
        );

        testPageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("getUserCards - Успешное получение карт пользователя")
    void getUserCards_shouldReturnCards_whenUserExists() {
        // Arrange
        Page<Card> cardPage = new PageImpl<>(List.of(testCard), testPageable, 1);

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findAllByOwner(eq(testUser), any(Pageable.class)))
                .thenReturn(cardPage);
        when(cardMapper.toDto(testCard))
                .thenReturn(testCardDto);

        // Act
        PageResponse<CardDto> result = apiCardService.getUserCards(testUserId, testPageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0)).isEqualTo(testCardDto);
        assertThat(result.totalElements()).isEqualTo(1);

        verify(userRepository).findByUniqueKey(testUserId);
        verify(cardRepository).findAllByOwner(testUser, testPageable);
        verify(cardMapper).toDto(testCard);
    }

    @Test
    @DisplayName("getUserCards - Бросает исключение при отсутствии пользователя")
    void getUserCards_shouldThrowException_whenUserNotFound() {
        // Arrange
        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> apiCardService.getUserCards(testUserId, testPageable))
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
        when(cardRepository.findAllByOwner(eq(testUser), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act
        PageResponse<CardDto> result = apiCardService.getUserCards(testUserId, testPageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(0);

        verify(userRepository).findByUniqueKey(testUserId);
        verify(cardRepository).findAllByOwner(testUser, testPageable);
        verifyNoInteractions(cardMapper);
    }

    @Test
    @DisplayName("getUserCards - Корректно передает параметры пагинации")
    void getUserCards_shouldPassPageableParamsCorrectly() {
        // Arrange
        Pageable customPageable = PageRequest.of(2, 5);
        Page<Card> cardPage = new PageImpl<>(List.of(testCard), customPageable, 10);

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findAllByOwner(eq(testUser), eq(customPageable)))
                .thenReturn(cardPage);
        when(cardMapper.toDto(testCard))
                .thenReturn(testCardDto);

        // Act
        PageResponse<CardDto> result = apiCardService.getUserCards(testUserId, customPageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.totalElements()).isEqualTo(11);

        verify(cardRepository).findAllByOwner(testUser, customPageable);
    }

    @Test
    @DisplayName("createCard - Успешное создание карты")
    void createCard_shouldReturnCardDto_whenRequestIsValid() {
        // Arrange
        Card savedCard = Card.builder()
                .id(2L)
                .cardNumber("4276550098765432")
                .owner(testUser)
                .balance(BigDecimal.ZERO)
                .validityPeriod(YearMonth.of(2029, 12))
                .status(com.banking.cards.common.CardStatus.ACTIVE)
                .build();

        CardDto expectedCardDto = new CardDto(
                new MaskedCardNumber(maskingConfig, "4276550098765432"),
                YearMonth.of(2029, 12),
                com.banking.cards.common.CardStatus.ACTIVE,
                new MaskedBalanceValue(maskingConfig, BigDecimal.ZERO)
        );
        when(adminCardService.getCard(testCreateCardRequest))
                .thenReturn(savedCard);
        when(cardRepository.save(savedCard))
                .thenReturn(savedCard);
        when(cardMapper.toDto(savedCard))
                .thenReturn(expectedCardDto);

        // Act
        CardDto result = apiCardService.createCard(testCreateCardRequest);

        // Assert
        assertThat(result).isEqualTo(expectedCardDto);

        verify(adminCardService).getCard(testCreateCardRequest);
        verify(cardRepository).save(savedCard);
        verify(cardMapper).toDto(savedCard);
        verify(auditService).log(
                eq(AuditAction.CARD_CREATED),
                eq(AuditEntityType.CARD),
                eq("4276550098765432"),
                eq("ownerUser=" + testUserId + " generated card by side service")
        );
    }

    @Test
    @DisplayName("createCard - Аудит логируется с правильными параметрами")
    void createCard_shouldLogAuditWithCorrectParameters() {
        // Arrange
        when(adminCardService.getCard(testCreateCardRequest))
                .thenReturn(testCard);
        when(cardRepository.save(testCard))
                .thenReturn(testCard);
        when(cardMapper.toDto(testCard))
                .thenReturn(testCardDto);

        // Act
        apiCardService.createCard(testCreateCardRequest);

        // Assert
        verify(auditService).log(
                eq(AuditAction.CARD_CREATED),
                eq(AuditEntityType.CARD),
                eq("4276550012345678"),
                eq("ownerUser=" + testUserId + " generated card by side service")
        );
    }

    @Test
    @DisplayName("createCard - Корректно обрабатывает карту с балансом")
    void createCard_shouldHandleCardWithBalanceCorrectly() {
        // Arrange
        AdminCreateCardRequest requestWithBalance = new AdminCreateCardRequest(
                testUserId,
                YearMonth.of(2030, 6),
                BigDecimal.valueOf(1000.50)
        );

        Card cardWithBalance = Card.builder()
                .id(3L)
                .cardNumber("5555555555554444")
                .owner(testUser)
                .balance(BigDecimal.valueOf(1000.50))
                .validityPeriod(YearMonth.of(2030, 6))
                .status(com.banking.cards.common.CardStatus.ACTIVE)
                .build();

        CardDto expectedDto = new CardDto(
                new MaskedCardNumber(maskingConfig, "5555555555554444"),
                YearMonth.of(2030, 6),
                com.banking.cards.common.CardStatus.ACTIVE,
                new MaskedBalanceValue(maskingConfig, BigDecimal.valueOf(1000.50))
        );

        when(adminCardService.getCard(requestWithBalance))
                .thenReturn(cardWithBalance);
        when(cardRepository.save(cardWithBalance))
                .thenReturn(cardWithBalance);
        when(cardMapper.toDto(cardWithBalance))
                .thenReturn(expectedDto);

        // Act
        CardDto result = apiCardService.createCard(requestWithBalance);

        // Assert
        assertThat(result).isEqualTo(expectedDto);
        assertThat(result.balance().value()).isEqualTo("1000.5");

        verify(auditService).log(
                eq(AuditAction.CARD_CREATED),
                eq(AuditEntityType.CARD),
                eq("5555555555554444"),
                eq("ownerUser=" + testUserId + " generated card by side service")
        );
    }

    @Test
    @DisplayName("createCard - Использует карту, сохраненную через AdminCardService")
    void createCard_shouldUseCardFromAdminCardService() {
        // Arrange
        Card cardFromAdminService = Card.builder()
                .cardNumber("1111222233334444")
                .owner(testUser)
                .build();

        when(adminCardService.getCard(testCreateCardRequest))
                .thenReturn(cardFromAdminService);
        when(cardRepository.save(cardFromAdminService))
                .thenReturn(cardFromAdminService);
        when(cardMapper.toDto(cardFromAdminService))
                .thenReturn(testCardDto);

        // Act
        apiCardService.createCard(testCreateCardRequest);

        // Assert
        verify(adminCardService).getCard(testCreateCardRequest);
        verify(cardRepository).save(cardFromAdminService);
        verify(cardMapper).toDto(cardFromAdminService);
    }

    @Test
    @DisplayName("createCard - Проверка цепочки вызовов")
    void createCard_shouldCallMethodsInCorrectOrder() {
        // Arrange
        when(adminCardService.getCard(testCreateCardRequest))
                .thenReturn(testCard);
        when(cardRepository.save(testCard))
                .thenReturn(testCard);
        when(cardMapper.toDto(testCard))
                .thenReturn(testCardDto);

        // Act
        apiCardService.createCard(testCreateCardRequest);

        // Assert
        verify(adminCardService).getCard(testCreateCardRequest);
        verify(cardRepository).save(testCard);
        verify(cardMapper).toDto(testCard);
        verify(auditService).log(any(), any(), any(), any());

        // Проверяем порядок вызовов
        inOrder(adminCardService, cardRepository, cardMapper, auditService).verify(adminCardService).getCard(testCreateCardRequest);
        inOrder(adminCardService, cardRepository, cardMapper, auditService).verify(cardRepository).save(testCard);
        inOrder(adminCardService, cardRepository, cardMapper, auditService).verify(cardMapper).toDto(testCard);
        inOrder(adminCardService, cardRepository, cardMapper, auditService).verify(auditService).log(any(), any(), any(), any());
    }
}