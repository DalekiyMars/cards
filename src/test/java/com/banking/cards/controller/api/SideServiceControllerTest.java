package com.banking.cards.controller.api;

import com.banking.cards.common.CardStatus;
import com.banking.cards.common.MaskedBalanceValue;
import com.banking.cards.common.MaskedCardNumber;
import com.banking.cards.config.MaskingConfig;
import com.banking.cards.constants.TestConstants;
import com.banking.cards.dto.request.AdminCreateCardRequest;
import com.banking.cards.dto.response.CardDto;
import com.banking.cards.dto.response.PageResponse;
import com.banking.cards.service.admin.AdminCardService;
import com.banking.cards.service.api.ApiCardService;
import com.banking.cards.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SideServiceController.class)
@AutoConfigureMockMvc(addFilters = false) // Отключаем JWT, аутентификацию для тестов
@Import(MaskingConfig.class)
class SideServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApiCardService apiCardService;

    @MockitoBean
    private AdminCardService adminCardService;

    @Autowired
    private MaskingConfig maskingConfig;

    @Autowired
    private ObjectMapper objectMapper;

    private final String JSON_PATH = TestConstants.BASE_CONTROLLER_PATH + "/sideService";

    /**
     * Получает валидный запрос на создание карты.
     */
    private AdminCreateCardRequest getValidCreateCardRequest() throws IOException {
        return JsonUtils.convertJsonFromFileToObject(
                JSON_PATH + "/SideServiceCreateCard.json",
                AdminCreateCardRequest.class
        );
    }

    /**
     * Получает запрос на создание карты с отрицательным балансом.
     */
    private AdminCreateCardRequest getNegativeBalanceCreateCardRequest() throws IOException {
        return JsonUtils.convertJsonFromFileToObject(
                JSON_PATH + "/SideServiceNegativeBalance.json",
                AdminCreateCardRequest.class
        );
    }

    /**
     * Получает запрос на создание карты с прошедшей датой.
     */
    private AdminCreateCardRequest getPastDateCreateCardRequest() throws IOException {
        return JsonUtils.convertJsonFromFileToObject(
                JSON_PATH + "/SideServicePastDate.json",
                AdminCreateCardRequest.class
        );
    }

    /**
     * Получает JSON с номером карты для удаления/изменения статуса.
     */
    private String getCardNumberRequest() throws IOException {
        return JsonUtils.readFile(JSON_PATH + "/SideServiceCardNumber.json");
    }

    /**
     * Получает ответ DTO карты.
     */
    private CardDto getCardDto() {
        return new CardDto(new MaskedCardNumber(maskingConfig, "4276550012345678"),
                YearMonth.of(2029, 12),
                CardStatus.ACTIVE,
                new MaskedBalanceValue(maskingConfig, BigDecimal.valueOf(0.0))
                );
    }

    /**
     * Получает UUID пользователя для тестов.
     */
    private UUID getTestUserId() {
        return UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    @DisplayName("POST /api/side-service - Успешное создание карты")
    void createCard_shouldReturn200_whenRequestIsValid() throws Exception {
        AdminCreateCardRequest request = getValidCreateCardRequest();
        CardDto expectedResponse = getCardDto();

        Mockito.when(apiCardService.createCard(any(AdminCreateCardRequest.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/api/side-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maskedNumber").value(expectedResponse.maskedNumber().value()))
                .andExpect(jsonPath("$.status").value(expectedResponse.status().toString()))
                .andExpect(jsonPath("$.balance").value(expectedResponse.balance().value()))
                .andExpect(jsonPath("$.validityPeriod").value(expectedResponse.validityPeriod().toString()));
    }

    @Test
    @DisplayName("POST /api/side-service - Ошибка 400 при отрицательном балансе")
    void createCard_shouldReturn400_whenBalanceIsNegative() throws Exception {
        AdminCreateCardRequest request = getNegativeBalanceCreateCardRequest();

        mockMvc.perform(post("/api/side-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/side-service - Ошибка 400 при прошедшей дате")
    void createCard_shouldReturn400_whenDateIsInPast() throws Exception {
        AdminCreateCardRequest request = getPastDateCreateCardRequest();

        mockMvc.perform(post("/api/side-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/side-service - Ошибка 404 при несуществующем пользователе")
    void createCard_shouldReturn404_whenUserNotFound() throws Exception {
        AdminCreateCardRequest request = getValidCreateCardRequest();

        Mockito.when(apiCardService.createCard(any(AdminCreateCardRequest.class)))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("User not found"));

        mockMvc.perform(post("/api/side-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("GET /api/side-service/{uuid} - Успешное получение карт пользователя")
    void getAllUserCards_shouldReturn200_whenUserExists() throws Exception {
        UUID userId = getTestUserId();
        CardDto cardDto = getCardDto();
        PageResponse<CardDto> pageResponse = new PageResponse<>(
                List.of(cardDto), 0, 20, 1, 1
        );

        Mockito.when(apiCardService.getUserCards(eq(userId), any(Pageable.class)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/side-service/{uuid}", userId)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].maskedNumber").value(cardDto.maskedNumber().value()))
                .andExpect(jsonPath("$.totalElements").value(pageResponse.totalElements()));
    }

    @Test
    @DisplayName("GET /api/side-service/{uuid} - Пагинация с ограничением размера страницы")
    void getAllUserCards_shouldLimitPageSizeTo50() throws Exception {
        UUID userId = getTestUserId();
        PageResponse<CardDto> pageResponse = new PageResponse<>(List.of(), 0, 50, 0, 0);

        Mockito.when(apiCardService.getUserCards(eq(userId), any(Pageable.class)))
                .thenReturn(pageResponse);

        // Проверяем, что при запросе size=100 контроллер ограничивает до 50
        mockMvc.perform(get("/api/side-service/{uuid}", userId)
                        .param("page", "0")
                        .param("size", "100"))
                .andExpect(status().isOk());

        // Проверяем, что сервис был вызван с size=50
        Mockito.verify(apiCardService).getUserCards(
                eq(userId),
                eq(PageRequest.of(0, 50, org.springframework.data.domain.Sort.by("id").ascending()))
        );
    }

    @Test
    @DisplayName("GET /api/side-service/{uuid} - Ошибка 404 при несуществующем пользователе")
    void getAllUserCards_shouldReturn404_whenUserNotFound() throws Exception {
        UUID userId = getTestUserId();

        Mockito.when(apiCardService.getUserCards(eq(userId), any(Pageable.class)))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("User not found"));

        mockMvc.perform(get("/api/side-service/{uuid}", userId)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("DELETE /api/side-service - Успешное удаление карты")
    void deleteCard_shouldReturn204_whenCardExists() throws Exception {
        String jsonBody = getCardNumberRequest();

        mockMvc.perform(delete("/api/side-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isNoContent());

        Mockito.verify(adminCardService).deleteCard("4276550012345678");
    }

    @Test
    @DisplayName("DELETE /api/side-service - Ошибка 404 при несуществующей карте")
    void deleteCard_shouldReturn404_whenCardNotFound() throws Exception {
        String jsonBody = getCardNumberRequest();

        Mockito.doThrow(new jakarta.persistence.EntityNotFoundException("Card not found"))
                .when(adminCardService)
                .deleteCard(any(String.class));

        mockMvc.perform(delete("/api/side-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Card not found"));
    }

    @Test
    @DisplayName("DELETE /api/side-service - Ошибка 403 при ненулевом балансе")
    void deleteCard_shouldReturn403_whenBalanceIsNotZero() throws Exception {
        String jsonBody = getCardNumberRequest();

        Mockito.doThrow(new com.banking.cards.exceptions.BadBalanceException("Card balance should be zero"))
                .when(adminCardService)
                .deleteCard(any(String.class));

        mockMvc.perform(delete("/api/side-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Card balance should be zero"));
    }

    @Test
    @DisplayName("PATCH /api/side-service/status - Успешное изменение статуса карты")
    void changeCardStatus_shouldReturn200_whenRequestIsValid() throws Exception {
        String jsonBody = getCardNumberRequest();

        mockMvc.perform(patch("/api/side-service/status")
                        .param("status", "BLOCKED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());

        Mockito.verify(adminCardService).changeStatus("4276550012345678", CardStatus.BLOCKED);
    }

    @Test
    @DisplayName("PATCH /api/side-service/status - Ошибка 404 при несуществующей карте")
    void changeCardStatus_shouldReturn404_whenCardNotFound() throws Exception {
        String jsonBody = getCardNumberRequest();

        Mockito.doThrow(new jakarta.persistence.EntityNotFoundException("Card not found"))
                .when(adminCardService)
                .changeStatus(any(String.class), any(CardStatus.class));

        mockMvc.perform(patch("/api/side-service/status")
                        .param("status", "BLOCKED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Card not found"));
    }

    @Test
    @DisplayName("PATCH /api/side-service/status - Ошибка 400 при невалидном статусе")
    void changeCardStatus_shouldReturn400_whenStatusIsInvalid() throws Exception {
        String jsonBody = getCardNumberRequest();

        mockMvc.perform(patch("/api/side-service/status")
                        .param("status", "INVALID_STATUS")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/side-service/status - Ошибка 400 при отсутствии параметра status")
    void changeCardStatus_shouldReturn400_whenStatusParamIsMissing() throws Exception {
        String jsonBody = getCardNumberRequest();

        mockMvc.perform(patch("/api/side-service/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/side-service - Ошибка 415 при неправильном Content-Type")
    void createCard_shouldReturn415_whenContentTypeIsWrong() throws Exception {
        AdminCreateCardRequest request = getValidCreateCardRequest();

        mockMvc.perform(post("/api/side-service")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.message").value("Content-Type not supported. Required: application/json"));
    }

    @Test
    @DisplayName("POST /api/side-service - Ошибка 400 при пустом теле запроса")
    void createCard_shouldReturn400_whenRequestBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/api/side-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/side-service - Ошибка 400 при невалидном номере карты")
    void deleteCard_shouldReturn400_whenCardNumberIsInvalid() throws Exception {
        String invalidCardNumber = "{\"cardNumber\": \"1234\"}";

        mockMvc.perform(delete("/api/side-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidCardNumber))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/side-service/status - Проверка неизменности статуса при совпадении")
    void changeCardStatus_shouldCallServiceEvenWhenStatusIsSame() throws Exception {
        String jsonBody = getCardNumberRequest();

        mockMvc.perform(patch("/api/side-service/status")
                        .param("status", "ACTIVE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());

        // Проверяем, что сервис был вызван даже при одинаковом статусе
        // (логика проверки совпадения внутри сервиса)
        Mockito.verify(adminCardService).changeStatus("4276550012345678", CardStatus.ACTIVE);
    }
}