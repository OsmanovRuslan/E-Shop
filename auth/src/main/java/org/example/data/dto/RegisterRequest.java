package org.example.data.dto;

/**
 * DTO для запроса на регистрацию пользователя.
 *
 * @param email Email пользователя
 * @param password Пароль пользователя
 * @param firstName Имя пользователя
 * @param lastName Фамилия пользователя
 * @param phone Телефон пользователя
 */
public record RegisterRequest(

        String email,

        String password,

        String firstName,

        String lastName,

        String phone
) {}