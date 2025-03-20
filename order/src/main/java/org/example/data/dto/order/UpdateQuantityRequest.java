package org.example.data.dto.order;

/**
 * DTO для запроса на обновление количества товара в заказе.
 *
 * @param quantity Новое количество товара
 */
public record UpdateQuantityRequest (

        Integer quantity

) {}
