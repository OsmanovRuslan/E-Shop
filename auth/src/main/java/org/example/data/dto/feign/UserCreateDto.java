package org.example.data.dto.feign;

/**
 * DTO для создания пользователя в User Service.
 *
 * @param firstName Имя пользователя
 * @param lastName Фамилия пользователя
 * @param email Email пользователя
 * @param phone Телефон пользователя
 */

public record UserCreateDto (

        String firstName,

        String lastName,

        String email,

        String phone

){}
