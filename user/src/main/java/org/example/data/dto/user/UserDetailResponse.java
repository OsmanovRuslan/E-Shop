package org.example.data.dto.user;

import org.example.data.dto.address.AddressResponse;

import java.util.Set;
import java.util.UUID;

/**
 * DTO для ответа с детальной информацией о пользователе.
 * Включает информацию об адресах пользователя.
 *
 * @param id Уникальный идентификатор пользователя
 * @param firstName Имя пользователя
 * @param lastName Фамилия пользователя
 * @param email Email пользователя
 * @param phone Телефон пользователя
 * @param addresses Набор адресов пользователя
 */
public record UserDetailResponse(

        UUID id,

        String firstName,

        String lastName,

        String email,

        String phone,

        Set<AddressResponse> addresses
) {}