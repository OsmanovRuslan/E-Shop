package org.example.data.dto.address;

/**
 * DTO для запроса на обновление адреса.
 *
 * @param street Новая улица и номер дома
 * @param city Новый город
 * @param country Новая страна
 * @param postalCode Новый почтовый индекс
 * @param isDefault Новый флаг, указывающий, должен ли адрес быть установлен как адрес по умолчанию
 */
public record AddressUpdateRequest(

        String street,

        String city,

        String country,

        String postalCode,

        Boolean isDefault
) {}
