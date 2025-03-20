package org.example.service.impl;

import org.example.data.NotificationEnum;
import org.example.data.NotificationStatus;
import org.example.data.dto.NotificationDetailsResponse;
import org.example.data.dto.NotificationListResponse;
import org.example.data.entity.NotificationEntity;
import org.example.data.mapper.NotificationMapper;
import org.example.exception.NotificationNotFoundException;
import org.example.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private JavaMailSender emailSender;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private UUID notificationId;
    private UUID userId;
    private NotificationEntity notificationEntity;
    private NotificationListResponse notificationListResponse;
    private NotificationDetailsResponse notificationDetailsResponse;
    private List<NotificationEntity> notificationEntities;
    private List<NotificationListResponse> notificationListResponses;
    private String toAddress;
    private String subject;
    private String message;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        notificationId = UUID.randomUUID();
        userId = UUID.randomUUID();
        now = LocalDateTime.now();
        toAddress = "user@example.com";
        subject = "Test Subject";
        message = "Test Message";

        notificationEntity = new NotificationEntity();
        notificationEntity.setId(notificationId);
        notificationEntity.setUserId(userId);
        notificationEntity.setType(NotificationEnum.ORDER_CONFIRMED);
        notificationEntity.setTitle("Подтверждение заказа");
        notificationEntity.setMessage("Ваш заказ подтвержден");
        notificationEntity.setStatus(NotificationStatus.SENT);
        notificationEntity.setIsRead(false);
        notificationEntity.setCreatedAt(now);
        notificationEntity.setUpdatedAt(now);

        notificationListResponse = new NotificationListResponse(
                notificationId,
                NotificationEnum.ORDER_CONFIRMED,
                "Подтверждение заказа",
                "Ваш заказ подтвержден",
                NotificationStatus.SENT,
                now,
                false
        );

        notificationDetailsResponse = new NotificationDetailsResponse(
                notificationId,
                NotificationEnum.ORDER_CONFIRMED,
                "Подтверждение заказа",
                "Ваш заказ подтвержден",
                NotificationStatus.SENT,
                now,
                now,
                false
        );

        NotificationEntity notificationEntity2 = new NotificationEntity();
        notificationEntity2.setId(UUID.randomUUID());
        notificationEntity2.setUserId(userId);
        notificationEntity2.setType(NotificationEnum.ORDER_CANCELLED);
        notificationEntity2.setTitle("Отмена заказа");
        notificationEntity2.setMessage("Ваш заказ отменен");
        notificationEntity2.setStatus(NotificationStatus.SENT);
        notificationEntity2.setIsRead(true);
        notificationEntity2.setCreatedAt(now);
        notificationEntity2.setUpdatedAt(now);

        notificationEntities = Arrays.asList(notificationEntity, notificationEntity2);

        NotificationListResponse notificationListResponse2 = new NotificationListResponse(
                notificationEntity2.getId(),
                NotificationEnum.ORDER_CANCELLED,
                "Отмена заказа",
                "Ваш заказ отменен",
                NotificationStatus.SENT,
                now,
                true
        );

        notificationListResponses = Arrays.asList(notificationListResponse, notificationListResponse2);
    }

    @Test
    void sendEmail_ShouldSendEmailSuccessfully() {
        when(notificationRepository.save(any(NotificationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailSender).send(any(SimpleMailMessage.class));

        notificationService.sendEmail(toAddress, subject, message, NotificationEnum.ORDER_CONFIRMED);

        verify(notificationRepository, times(2)).save(any(NotificationEntity.class));
        verify(emailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void getUserNotifications_ShouldReturnAllUserNotifications() {
        Sort sort = Sort.by(Sort.Direction.DESC, "updatedAt");
        when(notificationRepository.findByUserId(userId, sort)).thenReturn(notificationEntities);
        when(notificationMapper.toNotificationListResponseList(notificationEntities)).thenReturn(notificationListResponses);

        List<NotificationListResponse> result = notificationService.getUserNotifications(userId);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertEquals(notificationListResponses, result);

        verify(notificationRepository).findByUserId(userId, sort);
        verify(notificationMapper).toNotificationListResponseList(notificationEntities);
    }

    @Test
    void getUserNotificationsByStatus_ShouldReturnUserNotificationsWithSpecifiedStatus() {
        Sort sort = Sort.by(Sort.Direction.DESC, "updatedAt");
        when(notificationRepository.findByUserIdAndStatus(userId, NotificationStatus.SENT, sort)).thenReturn(notificationEntities);
        when(notificationMapper.toNotificationListResponseList(notificationEntities)).thenReturn(notificationListResponses);

        List<NotificationListResponse> result = notificationService.getUserNotificationsByStatus(userId, NotificationStatus.SENT);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertEquals(notificationListResponses, result);

        verify(notificationRepository).findByUserIdAndStatus(userId, NotificationStatus.SENT, sort);
        verify(notificationMapper).toNotificationListResponseList(notificationEntities);
    }

    @Test
    void getNotificationDetails_ShouldReturnNotificationDetails() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notificationEntity));
        when(notificationMapper.toNotificationDetailsResponse(notificationEntity)).thenReturn(notificationDetailsResponse);

        NotificationDetailsResponse result = notificationService.getNotificationDetails(notificationId);

        assertThat(result).isNotNull();
        assertEquals(notificationDetailsResponse, result);

        verify(notificationRepository).findById(notificationId);
        verify(notificationMapper).toNotificationDetailsResponse(notificationEntity);
    }

    @Test
    void getNotificationDetails_WhenNotificationNotFound_ShouldThrowException() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class, () ->
                notificationService.getNotificationDetails(notificationId)
        );

        verify(notificationRepository).findById(notificationId);
        verifyNoInteractions(notificationMapper);
    }

    @Test
    void markNotificationAsRead_ShouldMarkNotificationAsRead() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notificationEntity));
        when(notificationRepository.save(notificationEntity)).thenReturn(notificationEntity);

        notificationService.markNotificationAsRead(notificationId);

        assertTrue(notificationEntity.getIsRead());

        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository).save(notificationEntity);
    }

    @Test
    void markNotificationAsRead_WhenNotificationNotFound_ShouldThrowException() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class, () ->
                notificationService.markNotificationAsRead(notificationId)
        );

        verify(notificationRepository).findById(notificationId);
        verifyNoMoreInteractions(notificationRepository);
    }

    @Test
    void countUnreadNotifications_ShouldReturnCountOfUnreadNotifications() {
        long expectedCount = 5L;
        when(notificationRepository.countByUserIdAndIsRead(userId, false)).thenReturn(expectedCount);

        long result = notificationService.countUnreadNotifications(userId);

        assertEquals(expectedCount, result);
        verify(notificationRepository).countByUserIdAndIsRead(userId, false);
    }
}