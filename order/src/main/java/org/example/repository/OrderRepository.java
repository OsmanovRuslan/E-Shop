package org.example.repository;

import org.example.data.dto.order.OrderStatus;
import org.example.data.entity.OrderEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с заказами в MongoDB.
 * Предоставляет методы для поиска, сохранения и обновления заказов.
 */
@Repository
public interface OrderRepository extends MongoRepository<OrderEntity, UUID> {

    /**
     * Поиск заказа по идентификатору пользователя и статусу.
     * Используется для получения активной корзины пользователя.
     *
     * @param userId Уникальный идентификатор пользователя
     * @param status Статус заказа
     * @return Опциональный объект заказа
     */
    Optional<OrderEntity> findByUserIdAndStatus(UUID userId, OrderStatus status);

    /**
     * Получение всех заказов пользователя.
     *
     * @param userId Уникальный идентификатор пользователя
     * @return Список заказов пользователя
     */
    List<OrderEntity> findAllByUserId(UUID userId);
}
