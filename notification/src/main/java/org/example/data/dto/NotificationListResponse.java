package org.example.data.dto;

import org.example.data.NotificationEnum;
import org.example.data.NotificationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для представления уведомления в списке уведомлений.
 * Содержит основную информацию об уведомлении для отображения в списке.
 *
 * @param id Уникальный идентификатор уведомления
 * @param type Тип уведомления
 * @param title Заголовок уведомления
 * @param preview Предварительный текст сообщения (сокращенный вариант)
 * @param status Статус доставки уведомления
 * @param updatedAt Дата и время последнего обновления уведомления
 * @param isRead Флаг, указывающий, прочитано ли уведомление
 */
public record NotificationListResponse (

        UUID id,

        NotificationEnum type,

        String title,

        String preview,

        NotificationStatus status,

        LocalDateTime updatedAt,

        boolean isRead

) {}
