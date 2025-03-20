package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.data.dto.NotificationDetailsResponse;
import org.example.data.dto.NotificationListResponse;
import org.example.data.entity.NotificationEntity;
import org.example.data.NotificationEnum;
import org.example.data.NotificationStatus;
import org.example.data.mapper.NotificationMapper;
import org.example.exception.NotificationNotFoundException;
import org.example.repository.NotificationRepository;
import org.example.service.NotificationService;
import org.springframework.data.domain.Sort;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Реализация интерфейса сервиса уведомлений.
 * Предоставляет методы для работы с уведомлениями, их отправки и управления.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender emailSender;
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    /**
     * {@inheritDoc}
     *
     * @throws MailException если произошла ошибка при отправке email
     */
    @Override
    public void sendEmail(String toAddress, String subject, String message, NotificationEnum type) {
        log.debug("Отправка email на адрес: {}, тема: {}", toAddress, subject);

        NotificationEntity notification = new NotificationEntity();
        notification.setUserId(UUID.randomUUID());
        notification.setType(type);
        notification.setMessage(message);
        notification.setStatus(NotificationStatus.PENDING);

        notification = notificationRepository.save(notification);

        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setTo(toAddress);
            simpleMailMessage.setSubject(subject);
            simpleMailMessage.setText(message);
            emailSender.send(simpleMailMessage);

            notification.setStatus(NotificationStatus.SENT);
            notificationRepository.save(notification);

            log.info("Email успешно отправлен на адрес: {}", toAddress);
        } catch (MailException e) {
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);

            log.error("Ошибка при отправке email на адрес: {}", toAddress, e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NotificationListResponse> getUserNotifications(UUID userId) {
        log.debug("Получение списка уведомлений для пользователя с id: {}", userId);

        Sort sort = Sort.by(Sort.Direction.DESC, "updatedAt");
        List<NotificationEntity> notifications = notificationRepository.findByUserId(userId, sort);

        return notificationMapper.toNotificationListResponseList(notifications);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NotificationListResponse> getUserNotificationsByStatus(UUID userId, NotificationStatus status) {
        log.debug("Получение списка уведомлений со статусом {} для пользователя с id: {}", status, userId);

        Sort sort = Sort.by(Sort.Direction.DESC, "updatedAt");
        List<NotificationEntity> notifications = notificationRepository.findByUserIdAndStatus(userId, status, sort);

        return notificationMapper.toNotificationListResponseList(notifications);
    }

    /**
     * {@inheritDoc}
     *
     * @throws NotificationNotFoundException если уведомление не найдено
     */
    @Override
    public NotificationDetailsResponse getNotificationDetails(UUID notificationId) {
        log.debug("Получение детальной информации об уведомлении с id: {}", notificationId);

        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    log.error("Уведомление с id: {} не найдено", notificationId);
                    return new NotificationNotFoundException("Уведомление не найдено");
                });

        return notificationMapper.toNotificationDetailsResponse(notification);
    }

    /**
     * {@inheritDoc}
     *
     * @throws NotificationNotFoundException если уведомление не найдено
     */
    @Override
    public void markNotificationAsRead(UUID notificationId) {
        log.debug("Отметка уведомления с id : {} как прочитанного", notificationId);

        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    log.error("Уведомление с id: {} не найдено", notificationId);
                    return new NotificationNotFoundException("Уведомление не найдено");
                });

        notification.setIsRead(true);
        notificationRepository.save(notification);

        log.info("Уведомление с id: {} отмечено как прочитанное", notificationId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long countUnreadNotifications(UUID userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

}
