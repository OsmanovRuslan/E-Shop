package org.example.data.dto.feign;

/**
 * DTO для обновления пользователя в Keycloak через Feign клиент.
 *
 * @param firstName Имя пользователя
 * @param lastName Фамилия пользователя
 */
public record UserUpdateDto(

    String firstName,

    String lastName

) {}
