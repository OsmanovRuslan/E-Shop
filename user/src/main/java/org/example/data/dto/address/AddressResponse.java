package org.example.data.dto.address;

import java.util.UUID;

/**
 * DTO для ответа с информацией об адресе.
 *
 * @param id Уникальный идентификатор адреса
 * @param street Улица и номер дома
 * @param city Город
 * @param country Страна
 * @param postalCode Почтовый индекс
 * @param isDefault Флаг, указывающий, является ли адрес адресом по умолчанию
 */
public record AddressResponse(

        UUID id,

        String street,

        String city,

        String country,

        String postalCode,

        Boolean isDefault
) {}
