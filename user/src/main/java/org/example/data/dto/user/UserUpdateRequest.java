package org.example.data.dto.user;

/**
 * DTO для запроса на обновление пользователя.
 *
 * @param firstName Новое имя пользователя
 * @param lastName Новая фамилия пользователя
 * @param phone Новый телефон пользователя
 */
public record UserUpdateRequest (

        String firstName,

        String lastName,

        String phone
){}
