package org.example.data.dto.order;

import java.util.List;
import java.util.UUID;

/**
 * DTO для запроса на добавление товаров в корзину.
 *
 * @param items Список товаров для добавления в корзину
 * @param addressId Уникальный идентификатор адреса доставки
 */
public record AddToCartRequest (

        List<OrderItemRequest> items,

        UUID addressId

) {}
