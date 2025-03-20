package org.example.data.dto.user;

import java.util.UUID;

/**
 * DTO для ответа с базовой информацией о пользователе.
 *
 * @param id Уникальный идентификатор пользователя
 * @param firstName Имя пользователя
 * @param lastName Фамилия пользователя
 * @param email Email пользователя
 * @param phone Телефон пользователя
 */
public record UserResponse (

        UUID id,

        String firstName,

        String lastName,

        String email,

        String phone
){}
