package org.example.data.dto.feign.user;

import java.util.Set;
import java.util.UUID;

/**
 * DTO с детальной информацией о пользователе, получаемое от User Service.
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