package com.banking.cards.controller.user;

import com.banking.cards.common.CardStatus;
import com.banking.cards.common.MaskedBalanceValue;
import com.banking.cards.common.MaskedCardNumber;
import com.banking.cards.config.MaskingConfig;
import com.banking.cards.constants.TestConstants;
import com.banking.cards.dto.response.CardDto;
import com.banking.cards.dto.response.CardOperationDto;
import com.banking.cards.dto.response.PageResponse;
import com.banking.cards.service.user.UserCardInfoService;
import com.banking.cards.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardInfoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(MaskingConfig.class)
class CardInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MaskingConfig maskingConfig;

    @MockitoBean
    private UserCardInfoService userCardInfoService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID testUserId;

    @BeforeEach
    void setUp(){
        testUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                testUserId, // principal как UUID
                null, // credentials
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * Получает JSON с номером карты.
     */
    private String getCardNumberRequest() throws IOException {
        return JsonUtils.readFile(TestConstants.BASE_CONTROLLER_PATH + "/user/CardNumber.json");
    }

    /**
     * Создает тестовый DTO карты.
     */
    private CardDto createTestCardDto() {
        return new CardDto(new MaskedCardNumber(maskingConfig, "4276550012345678"),
                YearMonth.of(2029, 12),
                CardStatus.ACTIVE,
                new MaskedBalanceValue(maskingConfig, BigDecimal.valueOf(0.0))
        );
    }

    /**
     * Создает тестовый DTO операции по карте.
     */
    private CardOperationDto createTestCardOperationDto() {
        return new CardOperationDto(
                com.banking.cards.common.CardOperationType.TRANSFER,
                BigDecimal.valueOf(500.00),
                new MaskedCardNumber(maskingConfig, "4276550012345678"),
                new MaskedCardNumber(maskingConfig, "4276550098765432"),
                java.time.Instant.now()
        );
    }

    @Test
    @DisplayName("GET /api/cards/info - Успешное получение всех карт пользователя")
    void getMyCards_shouldReturn200_whenUserHasCards() throws Exception {
        CardDto cardDto = createTestCardDto();
        PageResponse<CardDto> pageResponse = new PageResponse<>(
                List.of(cardDto), 0, 10, 1, 1
        );

        Mockito.when(userCardInfoService.getUserCards(eq(testUserId), eq(0), eq(10)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/cards/info")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].maskedNumber").value(cardDto.maskedNumber().value()))
                .andExpect(jsonPath("$.content[0].status").value(cardDto.status().toString()))
                .andExpect(jsonPath("$.content[0].balance").value(cardDto.balance().value()))
                .andExpect(jsonPath("$.content[0].validityPeriod").value(cardDto.validityPeriod().toString()))
                .andExpect(jsonPath("$.totalElements").value(pageResponse.totalElements()));
    }

    @Test
    @DisplayName("GET /api/cards/info - Успешное получение пустого списка карт")
    void getMyCards_shouldReturn200_whenUserHasNoCards() throws Exception {
        PageResponse<CardDto> pageResponse = new PageResponse<>(List.of(), 0, 10, 0, 0);

        Mockito.when(userCardInfoService.getUserCards(eq(testUserId), eq(0), eq(10)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/cards/info")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("GET /api/cards/info - Использует параметры пагинации по умолчанию")
    void getMyCards_shouldUseDefaultPagination_whenParamsNotProvided() throws Exception {
        PageResponse<CardDto> pageResponse = new PageResponse<>(List.of(), 0, 10, 0, 0);

        Mockito.when(userCardInfoService.getUserCards(eq(testUserId), eq(0), eq(10)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/cards/info"))
                .andExpect(status().isOk());

        Mockito.verify(userCardInfoService).getUserCards(testUserId, 0, 10);
    }

    @Test
    @DisplayName("GET /api/cards/info - Использует кастомные параметры пагинации")
    void getMyCards_shouldUseCustomPaginationParams() throws Exception {
        PageResponse<CardDto> pageResponse = new PageResponse<>(List.of(), 1, 5, 5, 2);

        Mockito.when(userCardInfoService.getUserCards(eq(testUserId), eq(1), eq(5)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/cards/info")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk());

        Mockito.verify(userCardInfoService).getUserCards(testUserId, 1, 5);
    }

    @Test
    @DisplayName("POST /api/cards/info/one - Успешное получение одной карты")
    void getMyCard_shouldReturn200_whenCardExistsAndBelongsToUser() throws Exception {
        String jsonBody = getCardNumberRequest();
        CardDto cardDto = createTestCardDto();

        Mockito.when(userCardInfoService.getUserCard(eq("4276550012345678"), eq(testUserId)))
                .thenReturn(cardDto);

        mockMvc.perform(post("/api/cards/info/one")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maskedNumber").value(cardDto.maskedNumber().value()))
                .andExpect(jsonPath("$.status").value(cardDto.status().toString()))
                .andExpect(jsonPath("$.balance").value(cardDto.balance().value()))
                .andExpect(jsonPath("$.validityPeriod").value(cardDto.validityPeriod().toString()));
    }

    @Test
    @DisplayName("POST /api/cards/info/one - Ошибка 404 при отсутствии карты у пользователя")
    void getMyCard_shouldReturn404_whenCardNotFoundForUser() throws Exception {
        String jsonBody = getCardNumberRequest();

        Mockito.when(userCardInfoService.getUserCard(eq("4276550012345678"), eq(testUserId)))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Card not found"));

        mockMvc.perform(post("/api/cards/info/one")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Card not found"));
    }

    @Test
    @DisplayName("POST /api/cards/info/one - Ошибка 404 при отсутствии пользователя")
    void getMyCard_shouldReturn404_whenUserNotFound() throws Exception {
        String jsonBody = getCardNumberRequest();

        Mockito.when(userCardInfoService.getUserCard(eq("4276550012345678"), eq(testUserId)))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("User not found"));

        mockMvc.perform(post("/api/cards/info/one")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("POST /api/cards/info/operations - Успешное получение операций по карте")
    void getCardOperations_shouldReturn200_whenCardExistsAndBelongsToUser() throws Exception {
        String jsonBody = getCardNumberRequest();
        CardOperationDto operationDto = createTestCardOperationDto();
        PageResponse<CardOperationDto> pageResponse = new PageResponse<>(
                List.of(operationDto), 0, 20, 1, 1
        );

        Mockito.when(userCardInfoService.getCardOperations(eq("4276550012345678"), eq(testUserId), eq(0), eq(20)))
                .thenReturn(pageResponse);

        mockMvc.perform(post("/api/cards/info/operations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value(operationDto.type().toString()))
                .andExpect(jsonPath("$.content[0].type").value(operationDto.type().toString()))
                .andExpect(jsonPath("$.content[0].amount").value(operationDto.amount().doubleValue()))
                .andExpect(jsonPath("$.totalElements").value(pageResponse.totalElements()));
    }

    @Test
    @DisplayName("POST /api/cards/info/operations - Использует параметры пагинации по умолчанию")
    void getCardOperations_shouldUseDefaultPagination_whenParamsNotProvided() throws Exception {
        String jsonBody = getCardNumberRequest();
        PageResponse<CardOperationDto> pageResponse = new PageResponse<>(List.of(), 0, 20, 0, 0);

        Mockito.when(userCardInfoService.getCardOperations(eq("4276550012345678"), eq(testUserId), eq(0), eq(20)))
                .thenReturn(pageResponse);

        mockMvc.perform(post("/api/cards/info/operations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());

        Mockito.verify(userCardInfoService).getCardOperations("4276550012345678", testUserId, 0, 20);
    }

    @Test
    @DisplayName("POST /api/cards/info/operations - Использует кастомные параметры пагинации")
    void getCardOperations_shouldUseCustomPaginationParams() throws Exception {
        String jsonBody = getCardNumberRequest();
        PageResponse<CardOperationDto> pageResponse = new PageResponse<>(List.of(), 2, 5, 10, 2);

        Mockito.when(userCardInfoService.getCardOperations(eq("4276550012345678"), eq(testUserId), eq(2), eq(5)))
                .thenReturn(pageResponse);

        mockMvc.perform(post("/api/cards/info/operations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk());

        Mockito.verify(userCardInfoService).getCardOperations("4276550012345678", testUserId, 2, 5);
    }

    @Test
    @DisplayName("POST /api/cards/info/operations - Ошибка 404 при отсутствии карты у пользователя")
    void getCardOperations_shouldReturn404_whenCardNotFoundForUser() throws Exception {
        String jsonBody = getCardNumberRequest();

        Mockito.when(userCardInfoService.getCardOperations(eq("4276550012345678"), eq(testUserId), eq(0), eq(20)))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Card not found"));

        mockMvc.perform(post("/api/cards/info/operations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Card not found"));
    }

    @Test
    @DisplayName("POST /api/cards/info/operations - Ошибка 404 при отсутствии пользователя")
    void getCardOperations_shouldReturn404_whenUserNotFound() throws Exception {
        String jsonBody = getCardNumberRequest();

        Mockito.when(userCardInfoService.getCardOperations(eq("4276550012345678"), eq(testUserId), eq(0), eq(20)))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("User not found"));

        mockMvc.perform(post("/api/cards/info/operations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("POST /api/cards/info/operations - Возвращает пустой список при отсутствии операций")
    void getCardOperations_shouldReturnEmptyList_whenNoOperations() throws Exception {
        String jsonBody = getCardNumberRequest();
        PageResponse<CardOperationDto> pageResponse = new PageResponse<>(List.of(), 0, 20, 0, 0);

        Mockito.when(userCardInfoService.getCardOperations(eq("4276550012345678"), eq(testUserId), eq(0), eq(20)))
                .thenReturn(pageResponse);

        mockMvc.perform(post("/api/cards/info/operations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}