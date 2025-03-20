package org.example.service;

import org.example.data.dto.NotificationDetailsResponse;
import org.example.data.dto.NotificationListResponse;
import org.example.data.NotificationEnum;
import org.example.data.NotificationStatus;
import org.example.exception.NotificationNotFoundException;
import org.springframework.mail.MailException;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс сервиса уведомлений.
 * Определяет методы для работы с уведомлениями пользователей.
 */
public interface NotificationService {

    /**
     * Отправка email-уведомления.
     *
     * @param toAddress email получателя
     * @param subject тема сообщения
     * @param message текст сообщения
     * @param type тип уведомления
     * @throws MailException если произошла ошибка при отправке email
     */
    void sendEmail(String toAddress, String subject, String message, NotificationEnum type);

    /**
     * Получение списка уведомлений пользователя, отсортированных по дате обновления (сначала новые).
     *
     * @param userId ID пользователя
     * @return список уведомлений пользователя
     */
    List<NotificationListResponse> getUserNotifications(UUID userId);

    /**
     * Получение списка уведомлений пользователя с фильтрацией по статусу.
     *
     * @param userId ID пользователя
     * @param status статус уведомления (SENT, FAILED, PENDING)
     * @return отфильтрованный список уведомлений пользователя
     */
    List<NotificationListResponse> getUserNotificationsByStatus(UUID userId, NotificationStatus status);

    /**
     * Получение детальной информации об уведомлении.
     *
     * @param notificationId ID уведомления
     * @return детальная информация об уведомлении
     * @throws NotificationNotFoundException если уведомление не найдено
     */
    NotificationDetailsResponse getNotificationDetails(UUID notificationId);

    /**
     * Отметка уведомления как прочитанного пользователем.
     *
     * @param notificationId ID уведомления
     * @throws NotificationNotFoundException если уведомление не найдено
     */
    void markNotificationAsRead(UUID notificationId);

    /**
     * Подсчет непрочитанных уведомлений пользователя.
     *
     * @param userId ID пользователя
     * @return количество непрочитанных уведомлений
     */
    long countUnreadNotifications(UUID userId);

}
