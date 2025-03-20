package org.example.repository;

import org.example.data.NotificationStatus;
import org.example.data.entity.NotificationEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с уведомлениями в базе данных.
 * Предоставляет методы для поиска, создания, обновления и удаления уведомлений.
 */
@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    /**
     * Получение уведомлений пользователя
     *
     * @param userId ID пользователя
     * @param sort параметры сортировки
     * @return список уведомлений
     */
    List<NotificationEntity> findByUserId(UUID userId, Sort sort);

    /**
     * Получение уведомлений пользователя с фильтрацией по статусу
     *
     * @param userId ID пользователя
     * @param status статус уведомления
     * @param sort параметры сортировки
     * @return список уведомлений с указанным статусом
     */
    List<NotificationEntity> findByUserIdAndStatus(UUID userId, NotificationStatus status, Sort sort);

    /**
     * Подсчет непрочитанных уведомлений пользователя
     *
     * @param userId ID пользователя
     * @return количество непрочитанных уведомлений
     */
    long countByUserIdAndIsRead(UUID userId, boolean isRead);
}
