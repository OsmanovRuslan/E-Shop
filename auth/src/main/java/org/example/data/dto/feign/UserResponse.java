package org.example.data.dto.feign;

import java.util.UUID;

/**
 * DTO для ответа от User Service при создании пользователя.
 *
 * @param id Уникальный идентификатор пользователя
 * @param firstName Имя пользователя
 * @param lastName Фамилия пользователя
 * @param email Email пользователя
 * @param phone Телефон пользователя
 */
public record UserResponse(

        UUID id,

        String firstName,

        String lastName,

        String email,

        String phone
){}
