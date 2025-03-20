package org.example.data.mapper;

import org.example.data.dto.order.OrderItemRequest;
import org.example.data.dto.order.OrderResponse;
import org.example.data.dto.order.OrderSummaryResponse;
import org.example.data.entity.OrderEntity;
import org.example.data.entity.OrderItemEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Интерфейс для маппинга между сущностями заказов и DTO.
 * Использует MapStruct для автоматической генерации реализации.
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

    /**
     * Преобразование сущности заказа в DTO для ответа API.
     *
     * @param entity Сущность заказа
     * @return DTO с полной информацией о заказе
     */
    OrderResponse toOrderResponse(OrderEntity entity);

    /**
     * Преобразование списка заказов в список сокращенной информации.
     *
     * @param entities Список сущностей заказа
     * @return Список DTO с сокращенной информацией о заказах
     */
    List<OrderSummaryResponse> toOrderSummaryResponseList(List<OrderEntity> entities);

    /**
     * Преобразование списка запросов на добавление в корзину в список сущностей.
     *
     * @param requests Список запросов на добавление товаров
     * @return Список сущностей элементов заказа
     */
    List<OrderItemEntity> toOrderItemEntityList(List<OrderItemRequest> requests);

}