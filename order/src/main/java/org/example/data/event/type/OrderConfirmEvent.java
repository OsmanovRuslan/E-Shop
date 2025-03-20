package org.example.data.event.type;

import lombok.*;
import org.example.data.event.OrderEvent;
import org.example.data.event.extra.OrderItem;

import java.util.List;
import java.util.UUID;

/**
 * Событие подтверждения заказа.
 * Содержит информацию о подтвержденном заказе для создания уведомления.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmEvent extends OrderEvent {

        private UUID orderId;

        private UUID userId;

        private String email;

        private String username;

        private List<OrderItem> items;

        private Double totalPrice;

        private String fullAddress;

}
