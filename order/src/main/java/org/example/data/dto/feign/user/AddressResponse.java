package org.example.data.dto.feign.user;

import java.util.UUID;

/**
 * DTO с информацией об адресе пользователя, получаемое от User Service.
 *
 * @param id Уникальный идентификатор адреса
 * @param street Улица и номер дома
 * @param city Город
 * @param country Страна
 * @param postalCode Почтовый индекс
 * @param isDefault Признак адреса по умолчанию
 */
public record AddressResponse(

        UUID id,

        String street,

        String city,

        String country,

        String postalCode,

        Boolean isDefault
) {}
