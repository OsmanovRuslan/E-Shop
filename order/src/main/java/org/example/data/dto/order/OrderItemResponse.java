package org.example.data.dto.order;

import java.util.UUID;

/**
 * DTO для представления элемента заказа в ответе API.
 *
 * @param productId Уникальный идентификатор продукта
 * @param quantity Количество единиц товара
 * @param price Цена за единицу товара
 * @param subtotal Общая стоимость позиции (количество * цена)
 */
public record OrderItemResponse (

        UUID productId,

        Integer quantity,

        Double price,

        Double subtotal

){}
