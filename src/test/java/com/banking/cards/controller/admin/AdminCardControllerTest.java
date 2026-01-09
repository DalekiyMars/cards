package com.banking.cards.controller.admin;

import com.banking.cards.common.CardStatus;
import com.banking.cards.constants.TestConstants;
import com.banking.cards.dto.request.AdminCreateCardRequest;
import com.banking.cards.dto.response.AdminCardDto;
import com.banking.cards.dto.response.PageResponse;
import com.banking.cards.service.admin.AdminCardService;
import com.banking.cards.util.JsonUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // <-- НОВЫЙ ИМПОРТ
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(AdminCardController.class)
// Отключаем JWT, Login
@AutoConfigureMockMvc(addFilters = false)
class AdminCardControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminCardService adminCardService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String JSON_PATH = TestConstants.BASE_CONTROLLER_PATH;

    private AdminCreateCardRequest getAdminCreateCardRequest() throws IOException {
        return JsonUtils.convertJsonFromFileToObject(JSON_PATH + "/AdminCreateCard.json", AdminCreateCardRequest.class);
    }

    private AdminCreateCardRequest getBadBalanceAdminCreateCardRequest() throws IOException {
        return JsonUtils.convertJsonFromFileToObject(JSON_PATH + "/BadBalanceCreateCard.json", AdminCreateCardRequest.class);
    }

    private String getCardNumber() throws IOException {
        return JsonUtils.readFile(JSON_PATH + "/CardNumber.json");
    }

    private AdminCreateCardRequest getBadTimeAdminCreateCardRequest() throws IOException {
        return JsonUtils.convertJsonFromFileToObject(JSON_PATH + "/BadTimeCreateCard.json", AdminCreateCardRequest.class);
    }

    private AdminCardDto getAdminCardDto() throws IOException {
        return JsonUtils.convertJsonFromFileToObject(JSON_PATH + "/AdminCardDto.json", AdminCardDto.class);
    }

    @Test
    @DisplayName("POST /api/admin/cards - Успешное создание карты")
    void createCard_shouldReturn200_whenRequestIsValid() throws Exception {
        AdminCreateCardRequest request = getAdminCreateCardRequest();

        AdminCardDto expectedResponse = getAdminCardDto();

        Mockito.when(adminCardService.createCard(any(AdminCreateCardRequest.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maskedNumber").value(expectedResponse.maskedNumber()))
                .andExpect(jsonPath("$.balance").value(expectedResponse.balance()))
                .andExpect(jsonPath("$.status").value(expectedResponse.status().toString()));
    }

    @Test
    @DisplayName("POST /api/admin/cards - Ошибка 400 при отрицательном балансе")
    void createCard_shouldReturn400_whenBalanceIsNegative() throws Exception {
        AdminCreateCardRequest request = getBadBalanceAdminCreateCardRequest();

        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/admin/cards - Ошибка 400 при прошедшей дате")
    void createCard_shouldReturn400_whenDateIsInPast() throws Exception {
        AdminCreateCardRequest request = getBadTimeAdminCreateCardRequest();

        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/admin/cards/status - Успешное изменение статуса")
    void changeStatus_shouldReturn202_whenRequestIsValid() throws Exception {
        String jsonBody = getCardNumber();

        mockMvc.perform(patch("/api/admin/cards/status")
                        .param("status", "BLOCKED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isAccepted());

        Mockito.verify(adminCardService).changeStatus("4276550012345678", CardStatus.BLOCKED);
    }

    @Test
    @DisplayName("GET /api/admin/cards/{uuid} - Успешное получение списка")
    void getUserCards_shouldReturn200_andList() throws Exception {
        UUID userId = UUID.randomUUID();

        AdminCardDto cardDto = getAdminCardDto();

        PageResponse<AdminCardDto> pageResponse = new PageResponse<>(
                List.of(cardDto), 0, 20, 1, 1
        );

        Mockito.when(adminCardService.getUserCards(eq(userId), any(Pageable.class)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/admin/cards/{uuid}", userId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].maskedNumber").value(cardDto.maskedNumber()))
                .andExpect(jsonPath("$.totalElements").value(pageResponse.totalElements()));
    }

    @Test
    @DisplayName("DELETE /api/admin/cards - Успешное удаление")
    void deleteCard_shouldReturn200() throws Exception {
        String jsonBody = getCardNumber();

        mockMvc.perform(delete("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());

        Mockito.verify(adminCardService).deleteCard("4276550012345678");
    }
}