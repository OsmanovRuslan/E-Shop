package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.data.dto.LoginRequest;
import org.example.data.dto.LoginResponse;
import org.example.data.dto.RegisterRequest;
import org.example.data.dto.feign.UserUpdateDto;
import org.example.exception.InvalidCredentialsException;
import org.example.exception.LoginException;
import org.example.exception.RegistrationException;
import org.example.exception.UserAlreadyExistsException;
import org.example.exception.UserNotFoundException;
import org.example.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String TEST_EMAIL = "user@example.com";
    private final String TEST_PASSWORD = "password123";
    private final String TEST_FIRST_NAME = "Василий";
    private final String TEST_LAST_NAME = "Тонкин";
    private final String TEST_PHONE = "+1234567890";
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private LoginResponse loginResponse;
    private UserUpdateDto userUpdateDto;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                TEST_EMAIL,
                TEST_PASSWORD,
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                TEST_PHONE
        );

        loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

        loginResponse = new LoginResponse(
                "test-access-token",
                "test-refresh-token",
                300L,
                "Bearer"
        );

        userUpdateDto = new UserUpdateDto(
                "Вячеслав",
                "Тонкин"
        );
    }

    @Test
    void register_ShouldReturnOk() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body("Пользователь успешно создан"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Пользователь успешно создан"));
    }

    @Test
    void register_WhenUserAlreadyExists_ShouldReturnUserAlreadyExists() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Пользователь с email: " + TEST_EMAIL + " уже зарегистрирован"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Пользователь с email: " + TEST_EMAIL + " уже зарегистрирован"));

    }

    @Test
    void register_ShouldReturnRegistrationException() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RegistrationException("Ошибка регистрации"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Ошибка регистрации"));
    }

    @Test
    void login_ShouldReturnOk() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(ResponseEntity.ok(loginResponse));
        
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("test-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("test-refresh-token"))
                .andExpect(jsonPath("$.expiresIn").value(300))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void login_ShouldReturnInvalidCredentials() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Неверные учетные данные"));
        
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Неверные учетные данные"));
    }

    @Test
    void login_ShouldReturnLoginException() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new LoginException("Ошибка входа"));
        
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Ошибка входа"));
    }

    @Test
    void updateUser_ShouldReturnOk() throws Exception {
        when(authService.updateUserInKeycloak(eq(TEST_EMAIL), any(UserUpdateDto.class)))
                .thenReturn(ResponseEntity.ok("Данные пользователя успешно обновлены"));
        
        mockMvc.perform(put("/api/v1/auth/users/{email}", TEST_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Данные пользователя успешно обновлены"));
    }

    @Test
    void updateUser_ShouldReturnUserNotFound() throws Exception {
        when(authService.updateUserInKeycloak(eq(TEST_EMAIL), any(UserUpdateDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден"));
        
        mockMvc.perform(put("/api/v1/auth/users/{email}", TEST_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Пользователь не найден"));
    }

    @Test
    void updateUser_ShouldReturnServerError() throws Exception {
        when(authService.updateUserInKeycloak(eq(TEST_EMAIL), any(UserUpdateDto.class)))
                .thenThrow(new RuntimeException("Ошибка обновления данных пользователя"));

        mockMvc.perform(put("/api/v1/auth/users/{email}", TEST_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Ошибка обновления данных пользователя"));
    }

    @Test
    void deleteUser_ShouldReturnOk() throws Exception {
        when(authService.deleteUserFromKeycloak(TEST_EMAIL))
                .thenReturn(ResponseEntity.ok("Пользователь успешно удален из Keycloak"));
        
        mockMvc.perform(delete("/api/v1/auth/users/{email}", TEST_EMAIL))
                .andExpect(status().isOk())
                .andExpect(content().string("Пользователь успешно удален из Keycloak"));
    }

    @Test
    void deleteUser_ShouldReturnUserNotFound() throws Exception {
        when(authService.deleteUserFromKeycloak(TEST_EMAIL))
                .thenThrow(new UserNotFoundException("Пользователь не найден в Keycloak"));
        
        mockMvc.perform(delete("/api/v1/auth/users/{email}", TEST_EMAIL))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Пользователь не найден в Keycloak"));
    }

    @Test
    void deleteUser_ShouldReturnServerError() throws Exception {
        when(authService.deleteUserFromKeycloak(TEST_EMAIL))
                .thenThrow(new RuntimeException("Ошибка удаления пользователя из Keycloak"));

        mockMvc.perform(delete("/api/v1/auth/users/{email}", TEST_EMAIL))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Ошибка удаления пользователя из Keycloak"));
    }
}