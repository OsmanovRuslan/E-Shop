package org.example.feign;

import org.example.data.dto.feign.UserCreateDto;
import org.example.data.dto.feign.UserResponse;
import org.example.data.dto.feign.UserUpdateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Клиент для взаимодействия с User Service через Feign.
 * Позволяет создавать пользователей в User Service.
 */
@FeignClient(name = "user-service")
public interface UserServiceClient {

    /**
     * Создает пользователя в User Service.
     *
     * @param requestDto Данные пользователя для создания
     * @return ResponseEntity с информацией о созданном пользователе
     */
    @PostMapping("/api/v1/users")
    ResponseEntity<UserResponse> createUser(@RequestBody UserCreateDto requestDto);

}
