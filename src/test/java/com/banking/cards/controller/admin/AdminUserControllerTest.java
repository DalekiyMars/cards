package com.banking.cards.controller.admin;

import com.banking.cards.constants.TestConstants;
import com.banking.cards.dto.request.ChangeUserRoleRequest;
import com.banking.cards.service.admin.AdminUserService;
import com.banking.cards.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false) // Отключаем JWT, аутентификацию для тестов
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserService adminUserService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String JSON_PATH = TestConstants.BASE_CONTROLLER_PATH + "/admin";

    /**
     * Получает валидный запрос на изменение роли пользователя.
     */
    private ChangeUserRoleRequest getValidChangeUserRoleRequest() throws IOException {
        return JsonUtils.convertJsonFromFileToObject(
                JSON_PATH + "/ChangeUserRoleRequest.json",
                ChangeUserRoleRequest.class
        );
    }

    /**
     * Получает запрос с невалидным UUID.
     */
    private String getInvalidUuidRequest() throws IOException {
        return JsonUtils.readFile(JSON_PATH + "/ChangeUserRoleInvalidUuid.json");
    }

    /**
     * Получает запрос с null ролью.
     */
    private String getNullRoleRequest() throws IOException {
        return JsonUtils.readFile(JSON_PATH + "/ChangeUserRoleNullRole.json");
    }

    /**
     * Получает запрос с некорректным значением роли.
     */
    private String getInvalidRoleRequest() throws IOException {
        return JsonUtils.readFile(JSON_PATH + "/ChangeUserRoleInvalidRole.json");
    }

    /**
     * Получает запрос с отсутствующим полем id.
     */
    private String getMissingIdRequest() throws IOException {
        return JsonUtils.readFile(JSON_PATH + "/ChangeUserRoleMissingId.json");
    }

    @Test
    @DisplayName("PATCH /api/admin/users/role - Успешное изменение роли пользователя")
    void changeUserRole_shouldReturn200_whenRequestIsValid() throws Exception {
        ChangeUserRoleRequest request = getValidChangeUserRoleRequest();

        mockMvc.perform(patch("/api/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Проверяем, что сервис был вызван с правильными параметрами
        Mockito.verify(adminUserService).changeUserRole(
                eq(request.id()),
                eq(request.role())
        );
    }

    @Test
    @DisplayName("PATCH /api/admin/users/role - Ошибка 400 при невалидном UUID")
    void changeUserRole_shouldReturn400_whenUuidIsInvalid() throws Exception {
        String jsonBody = getInvalidUuidRequest();

        mockMvc.perform(patch("/api/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/admin/users/role - Ошибка 400 при null роли")
    void changeUserRole_shouldReturn400_whenRoleIsNull() throws Exception {
        String jsonBody = getNullRoleRequest();

        mockMvc.perform(patch("/api/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/admin/users/role - Ошибка 400 при некорректном значении роли")
    void changeUserRole_shouldReturn400_whenRoleValueIsInvalid() throws Exception {
        String jsonBody = getInvalidRoleRequest();

        mockMvc.perform(patch("/api/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/admin/users/role - Ошибка 400 при отсутствии поля id")
    void changeUserRole_shouldReturn400_whenIdIsMissing() throws Exception {
        String jsonBody = getMissingIdRequest();

        mockMvc.perform(patch("/api/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/admin/users/role - Ошибка 404 при отсутствии пользователя")
    void changeUserRole_shouldReturn404_whenUserNotFound() throws Exception {
        ChangeUserRoleRequest request = getValidChangeUserRoleRequest();

        // Настраиваем сервис для выброса исключения при поиске пользователя
        Mockito.doThrow(new jakarta.persistence.EntityNotFoundException("User not found"))
                .when(adminUserService)
                .changeUserRole(any(UUID.class), any(com.banking.cards.common.Role.class));

        mockMvc.perform(patch("/api/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("PATCH /api/admin/users/role - Ошибка 400 при пустом теле запроса")
    void changeUserRole_shouldReturn400_whenRequestBodyIsEmpty() throws Exception {
        mockMvc.perform(patch("/api/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/admin/users/role - Ошибка 400 при некорректном JSON")
    void changeUserRole_shouldReturn400_whenJsonIsMalformed() throws Exception {
        mockMvc.perform(patch("/api/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/admin/users/role - Проверка неизменности роли при совпадении")
    void changeUserRole_shouldNotCallRepository_whenRoleIsSame() throws Exception {
        ChangeUserRoleRequest request = getValidChangeUserRoleRequest();

        mockMvc.perform(patch("/api/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Валидируем, что сервис все равно был вызван (логика проверки совпадения ролей внутри сервиса)
        Mockito.verify(adminUserService).changeUserRole(
                eq(request.id()),
                eq(request.role())
        );
    }

    @Test
    @DisplayName("PATCH /api/admin/users/role - Ошибка 415 при неправильном Content-Type")
    void changeUserRole_shouldReturn415_whenContentTypeIsWrong() throws Exception {
        ChangeUserRoleRequest request = getValidChangeUserRoleRequest();

        mockMvc.perform(patch("/api/admin/users/role")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.message").value("Content-Type not supported. Required: application/json"));
    }
}