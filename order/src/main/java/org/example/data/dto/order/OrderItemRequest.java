package org.example.data.dto.order;

import java.util.UUID;

/**
 * DTO для запроса на добавление товара в заказ.
 *
 * @param productId Уникальный идентификатор продукта
 * @param quantity Количество единиц товара
 * @param price Цена за единицу товара
 */
public record OrderItemRequest (

        UUID productId,

        Integer quantity,

        Double price

){}
