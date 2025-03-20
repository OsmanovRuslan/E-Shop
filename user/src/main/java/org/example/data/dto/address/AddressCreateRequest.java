package org.example.data.dto.address;

/**
 * DTO для запроса на создание адреса.
 *
 * @param street Улица и номер дома
 * @param city Город
 * @param country Страна
 * @param postalCode Почтовый индекс
 * @param isDefault Флаг, указывающий, должен ли адрес быть установлен как адрес по умолчанию
 */
public record AddressCreateRequest(

        String street,

        String city,

        String country,

        String postalCode,

        Boolean isDefault
) {}