package org.example.data.dto.feign;

/**
 * DTO для обновления данных пользователя.
 *
 * @param firstName Новое имя пользователя
 * @param lastName Новая фамилия пользователя
 */
public record UserUpdateDto(

    String firstName,

    String lastName

) {}
