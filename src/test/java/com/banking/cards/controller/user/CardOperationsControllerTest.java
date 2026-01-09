package com.banking.cards.controller.user;

import com.banking.cards.config.MaskingConfig;
import com.banking.cards.constants.TestConstants;
import com.banking.cards.service.user.UserCardOperationService;
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
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardOperationsController.class)
@AutoConfigureMockMvc(addFilters = false) // Отключаем JWT, аутентификацию для тестов
@Import(MaskingConfig.class)
class CardOperationsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MaskingConfig maskingConfig;

    @MockitoBean
    private UserCardOperationService userCardOperationService;

    private final String JSON_PATH = TestConstants.BASE_CONTROLLER_PATH + "/user";

    @Autowired
    private ObjectMapper objectMapper;

    private final String testUserId = "550e8400-e29b-41d4-a716-446655440000";

    @BeforeEach
    void setUp(){

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                testUserId,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * Получает JSON для пополнения карты.
     */
    private String getDepositRequest() throws IOException {
        return JsonUtils.readFile(JSON_PATH + "/CardDepositRequest.json");
    }

    /**
     * Получает JSON для снятия средств.
     */
    private String getWithdrawRequest() throws IOException {
        return JsonUtils.readFile(JSON_PATH + "/CardWithdrawRequest.json");
    }

    /**
     * Получает JSON для перевода средств.
     */
    private String getTransferRequest() throws IOException {
        return JsonUtils.readFile(JSON_PATH + "/CardTransferRequest.json");
    }

    @Test
    @DisplayName("POST /api/cards/deposit - Успешное пополнение карты")
    void deposit_shouldReturn200_whenRequestIsValid() throws Exception {
        String jsonBody = getDepositRequest();

        mockMvc.perform(post("/api/cards/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());

        Mockito.verify(userCardOperationService).deposit(
                eq("4276550012345678"),
                eq(BigDecimal.valueOf(1000.00)),
                eq(UUID.fromString(testUserId))
        );
    }

    @Test
    @DisplayName("POST /api/cards/deposit - Ошибка 404 при отсутствии карты у пользователя")
    void deposit_shouldReturn404_whenCardNotFoundForUser() throws Exception {
        String jsonBody = getDepositRequest();

        Mockito.doThrow(new jakarta.persistence.EntityNotFoundException("Card not found"))
                .when(userCardOperationService)
                .deposit(any(String.class), any(BigDecimal.class), any(UUID.class));

        mockMvc.perform(post("/api/cards/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Card not found"));
    }

    @Test
    @DisplayName("POST /api/cards/deposit - Ошибка 409 при неактивной карте")
    void deposit_shouldReturn409_whenCardIsNotActive() throws Exception {
        String jsonBody = getDepositRequest();

        Mockito.doThrow(new IllegalStateException("Card is not active"))
                .when(userCardOperationService)
                .deposit(any(String.class), any(BigDecimal.class), any(UUID.class));

        mockMvc.perform(post("/api/cards/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Card is not active"));
    }

    @Test
    @DisplayName("POST /api/cards/withdraw - Успешное снятие средств")
    void withdraw_shouldReturn200_whenRequestIsValid() throws Exception {
        String jsonBody = getWithdrawRequest();

        mockMvc.perform(post("/api/cards/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());

        Mockito.verify(userCardOperationService).withdraw(
                eq("4276550012345678"),
                eq(BigDecimal.valueOf(500.00)),
                eq(UUID.fromString(testUserId))
        );
    }

    @Test
    @DisplayName("POST /api/cards/withdraw - Ошибка 404 при отсутствии карты у пользователя")
    void withdraw_shouldReturn404_whenCardNotFoundForUser() throws Exception {
        String jsonBody = getWithdrawRequest();

        Mockito.doThrow(new jakarta.persistence.EntityNotFoundException("Card not found"))
                .when(userCardOperationService)
                .withdraw(any(String.class), any(BigDecimal.class), any(UUID.class));

        mockMvc.perform(post("/api/cards/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Card not found"));
    }

    @Test
    @DisplayName("POST /api/cards/withdraw - Ошибка 409 при неактивной карте")
    void withdraw_shouldReturn409_whenCardIsNotActive() throws Exception {
        String jsonBody = getWithdrawRequest();

        Mockito.doThrow(new IllegalStateException("Card is not active"))
                .when(userCardOperationService)
                .withdraw(any(String.class), any(BigDecimal.class), any(UUID.class));

        mockMvc.perform(post("/api/cards/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Card is not active"));
    }

    @Test
    @DisplayName("POST /api/cards/withdraw - Ошибка 409 при недостаточных средствах")
    void withdraw_shouldReturn409_whenInsufficientFunds() throws Exception {
        String jsonBody = getWithdrawRequest();

        Mockito.doThrow(new IllegalStateException("Insufficient funds"))
                .when(userCardOperationService)
                .withdraw(any(String.class), any(BigDecimal.class), any(UUID.class));

        mockMvc.perform(post("/api/cards/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Insufficient funds"));
    }

    @Test
    @DisplayName("POST /api/cards/transfer - Успешный перевод средств")
    void transfer_shouldReturn200_whenRequestIsValid() throws Exception {
        String jsonBody = getTransferRequest();

        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());

        Mockito.verify(userCardOperationService).transfer(
                eq("4276550012345678"),
                eq("4276550098765432"),
                eq(BigDecimal.valueOf(250.00)),
                eq(UUID.fromString(testUserId))
        );
    }

    @Test
    @DisplayName("POST /api/cards/transfer - Ошибка 404 при отсутствии карты отправителя")
    void transfer_shouldReturn404_whenFromCardNotFound() throws Exception {
        String jsonBody = getTransferRequest();

        Mockito.doThrow(new jakarta.persistence.EntityNotFoundException("Card not found"))
                .when(userCardOperationService)
                .transfer(any(String.class), any(String.class), any(BigDecimal.class), any(UUID.class));

        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Card not found"));
    }

    @Test
    @DisplayName("POST /api/cards/transfer - Ошибка 404 при отсутствии карты получателя")
    void transfer_shouldReturn404_whenToCardNotFound() throws Exception {
        String jsonBody = getTransferRequest();

        Mockito.doThrow(new jakarta.persistence.EntityNotFoundException("Target card not found"))
                .when(userCardOperationService)
                .transfer(any(String.class), any(String.class), any(BigDecimal.class), any(UUID.class));

        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Target card not found"));
    }

    @Test
    @DisplayName("POST /api/cards/transfer - Ошибка 409 при неактивной карте отправителя")
    void transfer_shouldReturn409_whenFromCardIsNotActive() throws Exception {
        String jsonBody = getTransferRequest();

        Mockito.doThrow(new IllegalStateException("Card is not active"))
                .when(userCardOperationService)
                .transfer(any(String.class), any(String.class), any(BigDecimal.class), any(UUID.class));

        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Card is not active"));
    }

    @Test
    @DisplayName("POST /api/cards/transfer - Ошибка 409 при неактивной карте получателя")
    void transfer_shouldReturn409_whenToCardIsNotActive() throws Exception {
        String jsonBody = getTransferRequest();

        Mockito.doThrow(new IllegalStateException("Target card is not active"))
                .when(userCardOperationService)
                .transfer(any(String.class), any(String.class), any(BigDecimal.class), any(UUID.class));

        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Target card is not active"));
    }

    @Test
    @DisplayName("POST /api/cards/transfer - Ошибка 409 при недостаточных средствах")
    void transfer_shouldReturn409_whenInsufficientFunds() throws Exception {
        String jsonBody = getTransferRequest();

        Mockito.doThrow(new IllegalStateException("Insufficient funds"))
                .when(userCardOperationService)
                .transfer(any(String.class), any(String.class), any(BigDecimal.class), any(UUID.class));

        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Insufficient funds"));
    }

    @Test
    @DisplayName("POST /api/cards/deposit - Ошибка 404 при отсутствии пользователя")
    void deposit_shouldReturn404_whenUserNotFound() throws Exception {
        String jsonBody = getDepositRequest();

        Mockito.doThrow(new jakarta.persistence.EntityNotFoundException("User not found"))
                .when(userCardOperationService)
                .deposit(any(String.class), any(BigDecimal.class), any(UUID.class));

        mockMvc.perform(post("/api/cards/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("POST /api/cards/withdraw - Ошибка 404 при отсутствии пользователя")
    void withdraw_shouldReturn404_whenUserNotFound() throws Exception {
        String jsonBody = getWithdrawRequest();

        Mockito.doThrow(new jakarta.persistence.EntityNotFoundException("User not found"))
                .when(userCardOperationService)
                .withdraw(any(String.class), any(BigDecimal.class), any(UUID.class));

        mockMvc.perform(post("/api/cards/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("POST /api/cards/transfer - Ошибка 404 при отсутствии пользователя")
    void transfer_shouldReturn404_whenUserNotFound() throws Exception {
        String jsonBody = getTransferRequest();

        Mockito.doThrow(new jakarta.persistence.EntityNotFoundException("User not found"))
                .when(userCardOperationService)
                .transfer(any(String.class), any(String.class), any(BigDecimal.class), any(UUID.class));

        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}