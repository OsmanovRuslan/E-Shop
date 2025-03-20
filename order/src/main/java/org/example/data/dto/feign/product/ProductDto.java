package org.example.data.dto.feign.product;

import java.util.UUID;

/**
 * DTO с информацией о продукте, получаемое от Product Service.
 *
 * @param id Уникальный идентификатор продукта
 * @param name Наименование продукта
 */
public record ProductDto(

        UUID id,

        String name

) {}
