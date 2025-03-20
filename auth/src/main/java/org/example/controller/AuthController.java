package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.data.dto.LoginRequest;
import org.example.data.dto.LoginResponse;
import org.example.data.dto.RegisterRequest;
import org.example.data.dto.feign.UserUpdateDto;
import org.example.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для обработки API-запросов, связанных с аутентификацией и управлением пользователями.
 * Предоставляет эндпоинты для регистрации, входа, обновления и удаления пользователей.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Регистрирует нового пользователя в системе.
     * Создает пользователя в Keycloak и отправляет данные пользователя в User Service.
     *
     * @param request Данные для регистрации пользователя
     * @return ResponseEntity с сообщением о результате операции
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        log.info("Получен запрос на создание пользователя с username: {}", request.email());
        ResponseEntity<String> responseEntity = authService.register(request);
        log.info(responseEntity.getBody());
        return responseEntity;
    }

    /**
     * Аутентифицирует пользователя и предоставляет токены доступа.
     *
     * @param request Данные для входа пользователя (email и пароль)
     * @return ResponseEntity с токенами доступа и обновления
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        log.info("Получен запрос на вход с username: {}", request.email());
        ResponseEntity<LoginResponse> response = authService.login(request);
        log.info("Пользователь с username: {} успешно выполнил вход", request.email());
        return response;
    }

    /**
     * Обновляет информацию о пользователе в Keycloak.
     *
     * @param email Email пользователя, данные которого нужно обновить
     * @param userUpdateDto Новые данные пользователя
     * @return ResponseEntity с сообщением о результате операции
     */
    @PutMapping("/users/{email}")
    public ResponseEntity<String> updateUser(@PathVariable String email, @RequestBody UserUpdateDto userUpdateDto) {
        log.info("Получен запрос на обновление пользователя с email: {}", email);
        ResponseEntity<String> responseEntity = authService.updateUserInKeycloak(email, userUpdateDto);
        log.info("Пользователь с email: {} обновлен", email);
        return responseEntity;
    }

    /**
     * Удаляет пользователя из Keycloak.
     *
     * @param email Email пользователя, которого нужно удалить
     * @return ResponseEntity с сообщением о результате операции
     */
    @DeleteMapping("/users/{email}")
    public ResponseEntity<String> deleteUser(@PathVariable String email) {
        log.info("Получен запрос на удаление пользователя с email: {}", email);
        ResponseEntity<String> responseEntity = authService.deleteUserFromKeycloak(email);
        log.info("Пользователь с email: {} удален из Keycloak", email);
        return responseEntity;
    }

}
