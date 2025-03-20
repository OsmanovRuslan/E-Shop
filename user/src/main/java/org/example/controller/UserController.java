package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.data.dto.user.UserCreateRequest;
import org.example.data.dto.user.UserDetailResponse;
import org.example.data.dto.user.UserResponse;
import org.example.data.dto.user.UserUpdateRequest;
import org.example.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для управления пользователями.
 * Предоставляет API для создания, получения, обновления и удаления пользователей.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    /**
     * Создание нового пользователя.
     * Доступно только аутентифицированным пользователям.
     *
     * @param request Данные для создания пользователя
     * @return ResponseEntity с информацией о созданном пользователе
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserCreateRequest request) {
        log.info("Получен запрос на создание пользователя с email: {}", request.email());
        UserResponse response = userService.createUser(request);
        log.info("Создан пользователь с id: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Получение списка всех пользователей.
     * Доступно только аутентифицированным пользователям.
     *
     * @return ResponseEntity со списком пользователей
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей");
        List<UserResponse> users = userService.getAllUsers();
        log.info("Получено {} пользователей", users.size());
        return ResponseEntity.ok(users);
    }

    /**
     * Получение пользователя по ID.
     * Доступно только аутентифицированным пользователям.
     *
     * @param userId Уникальный идентификатор пользователя
     * @return ResponseEntity с информацией о пользователе
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") UUID userId) {
        log.info("Получен запрос на получение пользователя с id: {}", userId);
        UserResponse response = userService.getUserById(userId);
        log.info("Получен пользователь с id: {}", userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Получение детальной информации о пользователе по ID.
     * Доступно только аутентифицированным пользователям.
     *
     * @param userId Уникальный идентификатор пользователя
     * @return ResponseEntity с детальной информацией о пользователе, включая адреса
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<UserDetailResponse> getUserDetailsById(@PathVariable("id") UUID userId) {
        log.info("Получен запрос на получение детальной информации о пользователе с id: {}", userId);
        UserDetailResponse response = userService.getUserDetailsById(userId);
        log.info("Получена детальная информация о пользователе с id: {}", userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Обновление пользователя.
     * Пользователи могут обновлять только свои данные.
     * Доступно только аутентифицированным пользователям.
     *
     * @param userId Уникальный идентификатор пользователя
     * @param request Данные для обновления пользователя
     * @return ResponseEntity с обновленной информацией о пользователе
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable("id") UUID userId, @RequestBody UserUpdateRequest request) {
        log.info("Получен запрос на обновление пользователя с id: {}", userId);
        UserResponse response = userService.updateUser(userId, request);
        log.info("Обновлен пользователь с id: {}", response.id());
        return ResponseEntity.ok(response);
    }

    /**
     * Удаление пользователя.
     * Доступно только пользователям с ролью ADMIN.
     *
     * @param userId Уникальный идентификатор пользователя
     * @return ResponseEntity без тела ответа
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") UUID userId) {
        log.info("Получен запрос на удаление пользователя с id: {}", userId);
        userService.deleteUser(userId);
        log.info("Удален пользователь с id: {}", userId);
        return ResponseEntity.noContent().build();
    }

}

