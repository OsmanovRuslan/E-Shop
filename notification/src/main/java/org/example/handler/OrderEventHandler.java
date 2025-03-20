package org.example.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.data.NotificationEnum;
import org.example.data.event.type.OrderCancelEvent;
import org.example.data.event.type.OrderConfirmEvent;
import org.example.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Обработчик событий заказов, поступающих из Kafka.
 * Создает и отправляет уведомления на основе событий заказов.
 */
@Slf4j
@Component
@KafkaListener(topics = "order-events-topic")
public class OrderEventHandler {

    @Autowired
    private NotificationService notificationService;

    /**
     * Обрабатывает событие подтверждения заказа.
     * Создает и отправляет уведомление о подтверждении заказа.
     *
     * @param orderConfirmEvent Событие подтверждения заказа
     */
    @KafkaHandler
    public void handle(OrderConfirmEvent orderConfirmEvent) {
        log.info("Получен OrderConfirmEvent: {}", orderConfirmEvent.getOrderId());
        StringBuilder message = new StringBuilder(String.format("Здравствуйте, %s!\nВаш заказ #%s подтвержден.\nСписок товаров:\n",
                orderConfirmEvent.getUsername(), orderConfirmEvent.getOrderId()));
        for (var item : orderConfirmEvent.getItems()) {
            message.append(String.format(" - %s x %d = %.2f\n", item.getName(), item.getQuantity() , item.getPrice()));
        }
        message.append(String.format("Итоговая сумма заказа: %.2f\n", orderConfirmEvent.getTotalPrice()));
        message.append(String.format("Заказ будет доставлен по адресу: %s", orderConfirmEvent.getFullAddress()));

        notificationService.sendEmail(orderConfirmEvent.getEmail(),
                String.format("Подтверждение заказа #%s",  orderConfirmEvent.getOrderId()),
                message.toString(), NotificationEnum.ORDER_CONFIRMED);
    }

    /**
     * Обрабатывает событие отмены заказа.
     * Создает и отправляет уведомление об отмене заказа.
     *
     * @param orderCancelEvent Событие отмены заказа
     */
    @KafkaHandler
    public void handle(OrderCancelEvent orderCancelEvent) {
        log.info("Получен OrderCancelEvent: {}", orderCancelEvent.getOrderId());
        String message = String.format("Здравствуйте, %s!\nВаш заказ #%s отменен.",
                orderCancelEvent.getUsername(), orderCancelEvent.getOrderId());
        notificationService.sendEmail(orderCancelEvent.getEmail(),
                String.format("Отмена заказа #%s",  orderCancelEvent.getOrderId()),
                message, NotificationEnum.ORDER_CANCELLED);
    }

}
