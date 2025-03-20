package org.example.data.dto.feign;

/**
 * DTO для восстановления пользователя в Keycloak через Feign клиент.
 *
 * @param email Email пользователя
 * @param firstName Имя пользователя
 * @param lastName Фамилия пользователя
 * @param password Пароль пользователя
 */
public record UserRestoreDto (

        String email,
        String firstName,
        String lastName,
        String password

){}
