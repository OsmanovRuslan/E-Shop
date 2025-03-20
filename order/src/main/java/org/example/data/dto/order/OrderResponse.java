package org.example.data.dto.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO для представления полной информации о заказе в ответе API.
 *
 * @param id Уникальный идентификатор заказа
 * @param userId Уникальный идентификатор пользователя
 * @param items Список элементов заказа
 * @param total Общая стоимость заказа
 * @param status Статус заказа
 * @param addressId Уникальный идентификатор адреса доставки
 * @param createdAt Дата и время создания заказа
 * @param updatedAt Дата и время последнего обновления заказа
 */
public record OrderResponse (

        UUID id,

        UUID userId,

        List<OrderItemResponse> items,

        Double total,

        OrderStatus status,

        UUID addressId,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {}
