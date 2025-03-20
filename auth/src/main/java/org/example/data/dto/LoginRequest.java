package org.example.data.dto;

/**
 * DTO для запроса на вход пользователя.
 *
 * @param email Email пользователя
 * @param password Пароль пользователя
 */
public record LoginRequest (

        String email,

        String password
) {}