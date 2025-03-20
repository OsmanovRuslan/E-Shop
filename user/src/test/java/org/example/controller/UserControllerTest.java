package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.data.dto.user.UserCreateRequest;
import org.example.data.dto.user.UserDetailResponse;
import org.example.data.dto.user.UserResponse;
import org.example.data.dto.user.UserUpdateRequest;
import org.example.exception.user.UserNotFoundException;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private UserResponse userResponse;
    private UserDetailResponse userDetailResponse;
    private UserCreateRequest createRequest;
    private UserUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        userResponse = new UserResponse(
                userId,
                "Василий",
                "Лопатин",
                "vasya.lopa@example.com",
                "+12345678901"
        );

        userDetailResponse = new UserDetailResponse(
                userId,
                "Василий",
                "Лопатин",
                "vasya.lopa@example.com",
                "+12345678901",
                Set.of()
        );

        createRequest = new UserCreateRequest(
                "Василий",
                "Лопатин",
                "vasya.lopa@example.com",
                "+12345678901"
        );

        updateRequest = new UserUpdateRequest(
                "Василий",
                "Тонкин",
                "+19876543210"
        );
    }

    @Test
    void createUser_ShouldReturnCreated() throws Exception {
        when(userService.createUser(createRequest)).thenReturn(userResponse);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.firstName").value("Василий"))
                .andExpect(jsonPath("$.lastName").value("Лопатин"))
                .andExpect(jsonPath("$.email").value("vasya.lopa@example.com"))
                .andExpect(jsonPath("$.phone").value("+12345678901"));

        verify(userService, times(1)).createUser(any(UserCreateRequest.class));
    }

    @Test
    void getUserById_ShouldReturnOk() throws Exception {
        when(userService.getUserById(userId)).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.firstName").value("Василий"))
                .andExpect(jsonPath("$.lastName").value("Лопатин"))
                .andExpect(jsonPath("$.email").value("vasya.lopa@example.com"))
                .andExpect(jsonPath("$.phone").value("+12345678901"));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        when(userService.getUserById(userId)).thenThrow(new UserNotFoundException("Пользователь с id: " + userId + " не найден"));

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void getAllUsers_ShouldReturnOk() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(userResponse));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(userId.toString()))
                .andExpect(jsonPath("$[0].firstName").value("Василий"))
                .andExpect(jsonPath("$[0].lastName").value("Лопатин"))
                .andExpect(jsonPath("$[0].email").value("vasya.lopa@example.com"))
                .andExpect(jsonPath("$[0].phone").value("+12345678901"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getUserDetailsById_ShouldReturnOk() throws Exception {
        when(userService.getUserDetailsById(userId)).thenReturn(userDetailResponse);

        mockMvc.perform(get("/api/v1/users/{id}/details", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.firstName").value("Василий"))
                .andExpect(jsonPath("$.lastName").value("Лопатин"))
                .andExpect(jsonPath("$.email").value("vasya.lopa@example.com"))
                .andExpect(jsonPath("$.phone").value("+12345678901"))
                .andExpect(jsonPath("$.addresses").isArray())
                .andExpect(jsonPath("$.addresses").isEmpty());

        verify(userService, times(1)).getUserDetailsById(userId);
    }

    @Test
    void getUserDetailsById_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        when(userService.getUserDetailsById(userId)).thenThrow(new UserNotFoundException("Пользователь с id: " + userId + " не найден"));

        mockMvc.perform(get("/api/v1/users/{id}/details", userId))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserDetailsById(userId);
    }

    @Test
    void updateUser_ShouldReturnOk() throws Exception {
        UserResponse updatedResponse = new UserResponse(
                userId,
                "Василий",
                "Тонкин",
                "vasya.lopa@example.com",
                "+19876543210"
        );

        when(userService.updateUser(userId, updateRequest)).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.firstName").value("Василий"))
                .andExpect(jsonPath("$.lastName").value("Тонкин"))
                .andExpect(jsonPath("$.email").value("vasya.lopa@example.com"))
                .andExpect(jsonPath("$.phone").value("+19876543210"));

        verify(userService, times(1)).updateUser(eq(userId), any(UserUpdateRequest.class));
    }

    @Test
    void updateUser_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        when(userService.updateUser(userId, updateRequest))
                .thenThrow(new UserNotFoundException("Пользователь с id: " + userId + " не найден"));

        mockMvc.perform(put("/api/v1/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).updateUser(eq(userId), any(UserUpdateRequest.class));
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        doThrow(new UserNotFoundException("Пользователь с id: " + userId + " не найден"))
                .when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).deleteUser(userId);
    }
}