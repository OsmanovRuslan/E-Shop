package org.example.data.dto;

import org.example.data.NotificationEnum;
import org.example.data.NotificationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для представления детальной информации об уведомлении.
 * Содержит полную информацию об уведомлении, включая полный текст сообщения.
 *
 * @param id Уникальный идентификатор уведомления
 * @param type Тип уведомления
 * @param title Заголовок уведомления
 * @param fullMessage Полный текст сообщения уведомления
 * @param status Статус доставки уведомления
 * @param createdAt Дата и время создания уведомления
 * @param updatedAt Дата и время последнего обновления уведомления
 * @param isRead Флаг, указывающий, прочитано ли уведомление
 */
public record NotificationDetailsResponse (

        UUID id,

        NotificationEnum type,

        String title,

        String fullMessage,

        NotificationStatus status,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        boolean isRead

){}
