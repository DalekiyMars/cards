package com.banking.cards.service.user;

import com.banking.cards.common.CardOperationType;
import com.banking.cards.common.MaskedBalanceValue;
import com.banking.cards.common.MaskedCardNumber;
import com.banking.cards.config.MaskingConfig;
import com.banking.cards.dto.response.CardDto;
import com.banking.cards.dto.response.CardOperationDto;
import com.banking.cards.dto.response.PageResponse;
import com.banking.cards.entity.Card;
import com.banking.cards.entity.CardOperation;
import com.banking.cards.entity.User;
import com.banking.cards.mapper.CardMapper;
import com.banking.cards.repository.CardOperationRepository;
import com.banking.cards.repository.CardRepository;
import com.banking.cards.repository.UserRepository;
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
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCardInfoServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardOperationRepository operationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private MaskingConfig maskingConfig;

    @InjectMocks
    private UserCardInfoService userCardInfoService;

    private UUID testUserId;
    private User testUser;
    private Card testCard;
    private CardDto testCardDto;
    private CardOperation testOperation;
    private CardOperationDto testOperationDto;

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
                .balance(new BigDecimal("1000.00"))
                .validityPeriod(YearMonth.of(2029, 12))
                .status(com.banking.cards.common.CardStatus.ACTIVE)
                .build();

        testCardDto = new CardDto(
                new MaskedCardNumber(maskingConfig, "4276550012345678"),
                YearMonth.of(2029, 12),
                com.banking.cards.common.CardStatus.ACTIVE,
                new MaskedBalanceValue(maskingConfig, new BigDecimal("1000.00"))
        );

        testOperation = CardOperation.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .type(CardOperationType.TRANSFER)
                .createdAt(Instant.now())
                .fromCard(testCard)
                .build();

        testOperationDto = new CardOperationDto(
                CardOperationType.TRANSFER,
                new BigDecimal("100.00"),
                new MaskedCardNumber(maskingConfig, "4276550012345678"),
                new MaskedCardNumber(maskingConfig, "4276550098765432"),
                Instant.now()
        );
    }

    @Test
    @DisplayName("getUserCards - Успешное получение карт пользователя")
    void getUserCards_shouldReturnCards_whenUserExists() {
        // Arrange
        int page = 0;
        int size = 10;
        PageRequest expectedPageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<Card> cardPage = new PageImpl<>(List.of(testCard), expectedPageable, 1);

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findAllByOwner(testUser, expectedPageable))
                .thenReturn(cardPage);
        when(cardMapper.toDto(testCard))
                .thenReturn(testCardDto);

        // Act
        PageResponse<CardDto> result = userCardInfoService.getUserCards(testUserId, page, size);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0)).isEqualTo(testCardDto);
        assertThat(result.page()).isEqualTo(page);
        assertThat(result.size()).isEqualTo(size);
        assertThat(result.totalPages()).isEqualTo(1);

        verify(userRepository).findByUniqueKey(testUserId);
        verify(cardRepository).findAllByOwner(testUser, expectedPageable);
        verify(cardMapper).toDto(testCard);
    }

    @Test
    @DisplayName("getUserCards - Бросает исключение при отсутствии пользователя")
    void getUserCards_shouldThrowException_whenUserNotFound() {
        // Arrange
        int page = 0;
        int size = 10;

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userCardInfoService.getUserCards(testUserId, page, size))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findByUniqueKey(testUserId);
        verifyNoInteractions(cardRepository, cardMapper);
    }

    @Test
    @DisplayName("getUserCards - Возвращает пустую страницу при отсутствии карт")
    void getUserCards_shouldReturnEmptyPage_whenUserHasNoCards() {
        // Arrange
        int page = 0;
        int size = 10;
        PageRequest expectedPageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<Card> emptyPage = new PageImpl<>(List.of(), expectedPageable, 0);

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findAllByOwner(testUser, expectedPageable))
                .thenReturn(emptyPage);

        // Act
        PageResponse<CardDto> result = userCardInfoService.getUserCards(testUserId, page, size);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(0);

        verify(userRepository).findByUniqueKey(testUserId);
        verify(cardRepository).findAllByOwner(testUser, expectedPageable);
        verifyNoInteractions(cardMapper);
    }

    @Test
    @DisplayName("getUserCards - Корректно обрабатывает пагинацию с разными параметрами")
    void getUserCards_shouldHandleDifferentPaginationParameters() {
        // Arrange
        int page = 2;
        int size = 5;
        PageRequest expectedPageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<Card> cardPage = new PageImpl<>(List.of(testCard), expectedPageable, 25);

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findAllByOwner(testUser, expectedPageable))
                .thenReturn(cardPage);
        when(cardMapper.toDto(testCard))
                .thenReturn(testCardDto);

        // Act
        PageResponse<CardDto> result = userCardInfoService.getUserCards(testUserId, page, size);

        // Assert
        assertThat(result.page()).isEqualTo(page);
        assertThat(result.totalPages()).isEqualTo(size);
        assertThat(result.totalElements()).isEqualTo(25);

        verify(cardRepository).findAllByOwner(testUser, expectedPageable);
    }

    @Test
    @DisplayName("getUserCard - Успешное получение конкретной карты пользователя")
    void getUserCard_shouldReturnCard_whenCardExistsAndBelongsToUser() {
        // Arrange
        String cardId = "4276550012345678";

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndOwner(cardId, testUser))
                .thenReturn(Optional.of(testCard));
        when(cardMapper.toDto(testCard))
                .thenReturn(testCardDto);

        // Act
        CardDto result = userCardInfoService.getUserCard(cardId, testUserId);

        // Assert
        assertThat(result).isEqualTo(testCardDto);

        verify(userRepository).findByUniqueKey(testUserId);
        verify(cardRepository).findByCardNumberAndOwner(cardId, testUser);
        verify(cardMapper).toDto(testCard);
    }

    @Test
    @DisplayName("getUserCard - Бросает исключение при отсутствии пользователя")
    void getUserCard_shouldThrowException_whenUserNotFound() {
        // Arrange
        String cardId = "4276550012345678";

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userCardInfoService.getUserCard(cardId, testUserId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findByUniqueKey(testUserId);
        verifyNoInteractions(cardRepository, cardMapper);
    }

    @Test
    @DisplayName("getUserCard - Бросает исключение при отсутствии карты")
    void getUserCard_shouldThrowException_whenCardNotFound() {
        // Arrange
        String cardId = "9999999999999999";

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndOwner(cardId, testUser))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userCardInfoService.getUserCard(cardId, testUserId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Card not found");

        verify(userRepository).findByUniqueKey(testUserId);
        verify(cardRepository).findByCardNumberAndOwner(cardId, testUser);
        verifyNoInteractions(cardMapper);
    }

    @Test
    @DisplayName("getUserCard - Бросает исключение при несовпадении владельца карты")
    void getUserCard_shouldThrowException_whenCardDoesNotBelongToUser() {
        // Arrange
        String cardId = "4276550012345678";

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndOwner(cardId, testUser))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userCardInfoService.getUserCard(cardId, testUserId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Card not found");

        verify(userRepository).findByUniqueKey(testUserId);
        verify(cardRepository).findByCardNumberAndOwner(cardId, testUser);
        verifyNoInteractions(cardMapper);
    }

    @Test
    @DisplayName("getCardOperations - Успешное получение операций по карте")
    void getCardOperations_shouldReturnOperations_whenCardExistsAndBelongsToUser() {
        // Arrange
        String cardId = "4276550012345678";
        int page = 0;
        int size = 10;
        PageRequest expectedPageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<CardOperation> operationPage = new PageImpl<>(List.of(testOperation), expectedPageable, 1);

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndOwner(cardId, testUser))
                .thenReturn(Optional.of(testCard));
        when(operationRepository.findAllByFromCard_IdOrToCard_Id(testCard.getId(), testCard.getId(), expectedPageable))
                .thenReturn(operationPage);
        when(cardMapper.toOperationDto(testOperation))
                .thenReturn(testOperationDto);

        // Act
        PageResponse<CardOperationDto> result = userCardInfoService.getCardOperations(cardId, testUserId, page, size);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0)).isEqualTo(testOperationDto);
        assertThat(result.page()).isEqualTo(page);
        assertThat(result.size()).isEqualTo(size);
        assertThat(result.totalElements()).isEqualTo(1);

        verify(userRepository).findByUniqueKey(testUserId);
        verify(cardRepository).findByCardNumberAndOwner(cardId, testUser);
        verify(operationRepository).findAllByFromCard_IdOrToCard_Id(testCard.getId(), testCard.getId(), expectedPageable);
        verify(cardMapper).toOperationDto(testOperation);
    }

    @Test
    @DisplayName("getCardOperations - Бросает исключение при отсутствии пользователя")
    void getCardOperations_shouldThrowException_whenUserNotFound() {
        // Arrange
        String cardId = "4276550012345678";
        int page = 0;
        int size = 10;

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userCardInfoService.getCardOperations(cardId, testUserId, page, size))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findByUniqueKey(testUserId);
        verifyNoInteractions(cardRepository, operationRepository, cardMapper);
    }

    @Test
    @DisplayName("getCardOperations - Бросает исключение при отсутствии карты")
    void getCardOperations_shouldThrowException_whenCardNotFound() {
        // Arrange
        String cardId = "9999999999999999";
        int page = 0;
        int size = 10;

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndOwner(cardId, testUser))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userCardInfoService.getCardOperations(cardId, testUserId, page, size))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Card not found");

        verify(userRepository).findByUniqueKey(testUserId);
        verify(cardRepository).findByCardNumberAndOwner(cardId, testUser);
        verifyNoInteractions(operationRepository, cardMapper);
    }

    @Test
    @DisplayName("getCardOperations - Возвращает операции, где карта является отправителем или получателем")
    void getCardOperations_shouldReturnOperationsWhereCardIsSenderOrReceiver() {
        // Arrange
        String cardId = "4276550012345678";
        int page = 0;
        int size = 10;
        PageRequest expectedPageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        CardOperation operationAsReceiver = CardOperation.builder()
                .id(2L)
                .amount(new BigDecimal("200.00"))
                .type(CardOperationType.TRANSFER)
                .createdAt(Instant.now())
                .toCard(testCard)
                .build();

        CardOperationDto operationDtoAsReceiver = new CardOperationDto(
                CardOperationType.TRANSFER,
                new BigDecimal("200.00"),
                new MaskedCardNumber(maskingConfig, "4276550098765432"),
                new MaskedCardNumber(maskingConfig, "4276550012345678"),
                Instant.now()
        );

        Page<CardOperation> operationPage = new PageImpl<>(List.of(testOperation, operationAsReceiver), expectedPageable, 2);

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndOwner(cardId, testUser))
                .thenReturn(Optional.of(testCard));
        when(operationRepository.findAllByFromCard_IdOrToCard_Id(testCard.getId(), testCard.getId(), expectedPageable))
                .thenReturn(operationPage);
        when(cardMapper.toOperationDto(testOperation))
                .thenReturn(testOperationDto);
        when(cardMapper.toOperationDto(operationAsReceiver))
                .thenReturn(operationDtoAsReceiver);

        // Act
        PageResponse<CardOperationDto> result = userCardInfoService.getCardOperations(cardId, testUserId, page, size);

        // Assert
        assertThat(result.content()).hasSize(2);
        verify(operationRepository).findAllByFromCard_IdOrToCard_Id(testCard.getId(), testCard.getId(), expectedPageable);
    }

    @Test
    @DisplayName("getCardOperations - Возвращает пустую страницу при отсутствии операций")
    void getCardOperations_shouldReturnEmptyPage_whenNoOperations() {
        // Arrange
        String cardId = "4276550012345678";
        int page = 0;
        int size = 10;
        PageRequest expectedPageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<CardOperation> emptyPage = new PageImpl<>(List.of(), expectedPageable, 0);

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndOwner(cardId, testUser))
                .thenReturn(Optional.of(testCard));
        when(operationRepository.findAllByFromCard_IdOrToCard_Id(testCard.getId(), testCard.getId(), expectedPageable))
                .thenReturn(emptyPage);

        // Act
        PageResponse<CardOperationDto> result = userCardInfoService.getCardOperations(cardId, testUserId, page, size);

        // Assert
        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(0);

        verify(operationRepository).findAllByFromCard_IdOrToCard_Id(testCard.getId(), testCard.getId(), expectedPageable);
        verifyNoInteractions(cardMapper);
    }

    @Test
    @DisplayName("getCardOperations - Корректно обрабатывает пагинацию с разными параметрами")
    void getCardOperations_shouldHandleDifferentPaginationParameters() {
        // Arrange
        String cardId = "4276550012345678";
        int page = 1;
        int size = 20;
        PageRequest expectedPageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<CardOperation> operationPage = new PageImpl<>(List.of(testOperation), expectedPageable, 50);

        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndOwner(cardId, testUser))
                .thenReturn(Optional.of(testCard));
        when(operationRepository.findAllByFromCard_IdOrToCard_Id(testCard.getId(), testCard.getId(), expectedPageable))
                .thenReturn(operationPage);
        when(cardMapper.toOperationDto(testOperation))
                .thenReturn(testOperationDto);

        // Act
        PageResponse<CardOperationDto> result = userCardInfoService.getCardOperations(cardId, testUserId, page, size);

        // Assert
        assertThat(result.page()).isEqualTo(page);
        assertThat(result.size()).isEqualTo(size);
        assertThat(result.totalElements()).isEqualTo(50);

        verify(operationRepository).findAllByFromCard_IdOrToCard_Id(testCard.getId(), testCard.getId(), expectedPageable);
    }

    @Test
    @DisplayName("getUser - Выбрасывает исключение при отсутствии пользователя")
    void getUser_shouldThrowException_whenUserNotFound() {
        // Arrange
        when(userRepository.findByUniqueKey(testUserId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userCardInfoService.getUserCards(testUserId, 0, 10))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findByUniqueKey(testUserId);
    }
}