package org.example.data.event.extra;

import lombok.Data;

/**
 * Класс, представляющий товар заказа.
 * Содержит информацию о наименовании, количестве и цене товара в заказе.
 */
@Data
public class OrderItem {

    private String name;

    private Integer quantity;

    private Double price;

}


