package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.data.NotificationStatus;
import org.example.data.dto.NotificationDetailsResponse;
import org.example.data.dto.NotificationListResponse;
import org.example.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для обработки API-запросов, связанных с уведомлениями пользователей.
 * Предоставляет эндпоинты для получения, обновления и управления уведомлениями.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Получение списка уведомлений пользователя.
     *
     * @param userId Уникальный идентификатор пользователя
     * @return ResponseEntity со списком уведомлений пользователя
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationListResponse>> getUserNotifications(@PathVariable UUID userId) {
        log.info("Получен запрос на получение списка уведомлений пользователя с id: {}", userId);
        List<NotificationListResponse> notifications = notificationService.getUserNotifications(userId);
        log.info("Получено {} уведомлений для пользователя с id: {}", notifications.size(), userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Получение списка уведомлений пользователя с фильтрацией по статусу.
     *
     * @param userId Уникальный идентификатор пользователя
     * @param status Статус уведомлений для фильтрации
     * @return ResponseEntity со списком отфильтрованных уведомлений пользователя
     */
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<NotificationListResponse>> getUserNotificationsByStatus(@PathVariable UUID userId, @PathVariable NotificationStatus status) {
        log.info("Получен запрос на получение списка уведомлений со статусом {} для пользователя с id: {}", status, userId);
        List<NotificationListResponse> notifications = notificationService.getUserNotificationsByStatus(userId, status);
        log.info("Получено {} уведомлений со статусом {} для пользователя с id: {}",
                notifications.size(), status, userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Получение детальной информации об уведомлении.
     *
     * @param notificationId Уникальный идентификатор уведомления
     * @return ResponseEntity с детальной информацией об уведомлении
     */
    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationDetailsResponse> getNotificationDetails(@PathVariable UUID notificationId) {
        log.info("Получен запрос на получение детальной информации об уведомлении с id: {}", notificationId);
        NotificationDetailsResponse notification = notificationService.getNotificationDetails(notificationId);
        log.info("Получена детальная информация об уведомлении с id: {}", notificationId);
        return ResponseEntity.ok(notification);
    }

    /**
     * Отметка уведомления как прочитанного.
     *
     * @param notificationId Уникальный идентификатор уведомления
     * @return ResponseEntity без тела ответа
     */
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable UUID notificationId) {
        log.info("Получен запрос на отметку уведомления с id: {} как прочитанного", notificationId);
        notificationService.markNotificationAsRead(notificationId);
        log.info("Уведомление с id: {} отмечено как прочитанное", notificationId);
        return ResponseEntity.ok().build();
    }

    /**
     * Получение количества непрочитанных уведомлений пользователя.
     *
     * @param userId Уникальный идентификатор пользователя
     * @return ResponseEntity с количеством непрочитанных уведомлений
     */
    @GetMapping("/user/{userId}/unread/count")
    public ResponseEntity<Long> getUnreadNotificationsCount(@PathVariable UUID userId) {
        log.info("Получен запрос на получение количества непрочитанных уведомлений пользователя с id: {}", userId);
        long count = notificationService.countUnreadNotifications(userId);
        log.info("Пользователь с id: {} имеет {} непрочитанных уведомлений", userId, count);
        return ResponseEntity.ok(count);
    }
}
