package org.example.data.dto.user;

/**
 * DTO для запроса на создание пользователя.
 *
 * @param firstName Имя пользователя
 * @param lastName Фамилия пользователя
 * @param email Email пользователя
 * @param phone Телефон пользователя
 */
public record UserCreateRequest (

        String firstName,

        String lastName,

        String email,

        String phone
) {}
