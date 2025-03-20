package org.example.feign;

import org.example.data.dto.feign.UserUpdateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Feign клиент для взаимодействия с Auth Service.
 * Предоставляет методы для обновления и удаления пользователей в Keycloak.
 */
@FeignClient(name = "auth-service")
public interface AuthServiceClient {

    /**
     * Обновление пользователя в Keycloak.
     *
     * @param email Email пользователя
     * @param userUpdateDto DTO с данными для обновления
     * @return ResponseEntity с сообщением о результате операции
     */
    @PutMapping("/api/v1/auth/users/{email}")
    ResponseEntity<String> updateUserInKeycloak(@PathVariable String email, @RequestBody UserUpdateDto userUpdateDto);

    /**
     * Удаление пользователя из Keycloak.
     *
     * @param email Email пользователя
     * @return ResponseEntity с сообщением о результате операции
     */
    @DeleteMapping("/api/v1/auth/users/{email}")
    ResponseEntity<String> deleteUserFromKeycloak(@PathVariable String email);
}