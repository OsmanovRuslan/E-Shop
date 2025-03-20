package org.example.data.dto.order;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для представления сокращенной информации о заказе в ответе API.
 *
 * @param id Уникальный идентификатор заказа
 * @param userId Уникальный идентификатор пользователя
 * @param itemCount Количество позиций в заказе
 * @param total Общая стоимость заказа
 * @param status Статус заказа
 * @param createdAt Дата и время создания заказа
 */
public record OrderSummaryResponse (

        UUID id,

        UUID userId,

        int itemCount,

        Double total,

        OrderStatus status,

        LocalDateTime createdAt

){}
