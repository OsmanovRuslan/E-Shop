package org.example.data.entity;

import lombok.*;
import org.example.data.dto.order.OrderStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Сущность заказа для хранения в MongoDB.
 * Содержит полную информацию о заказе, его элементах и статусе.
 */
@Data
@Document(collection = "orders")
public class OrderEntity {

    @Id
    private UUID id;

    private UUID userId;

    private List<OrderItemEntity> items;

    private Double total;

    private OrderStatus status;

    private UUID addressId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
