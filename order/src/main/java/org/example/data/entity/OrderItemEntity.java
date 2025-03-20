package org.example.data.entity;

import lombok.Data;

import java.util.UUID;

/**
 * Сущность, представляющая элемент заказа.
 * Содержит информацию о товаре, его количестве и цене.
 */
@Data
public class OrderItemEntity {

    private UUID productId;

    private Integer quantity;

    private Double price;

}
