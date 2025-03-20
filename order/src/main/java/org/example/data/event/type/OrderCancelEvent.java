package org.example.data.event.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.data.event.OrderEvent;

import java.util.UUID;

/**
 * Событие отмены заказа.
 * Содержит информацию об отмененном заказе для создания уведомления.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelEvent extends OrderEvent {

    private UUID orderId;

    private UUID userId;

    private String email;

    private String username;

    private Double totalPrice;

}
