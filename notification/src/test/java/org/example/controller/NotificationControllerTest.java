package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.data.NotificationEnum;
import org.example.data.NotificationStatus;
import org.example.data.dto.NotificationDetailsResponse;
import org.example.data.dto.NotificationListResponse;
import org.example.exception.NotificationNotFoundException;
import org.example.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID notificationId;
    private UUID userId;
    private NotificationDetailsResponse notificationDetailsResponse;
    private List<NotificationListResponse> notificationListResponses;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        notificationId = UUID.randomUUID();
        userId = UUID.randomUUID();
        now = LocalDateTime.now();

        NotificationListResponse notification1 = new NotificationListResponse(
                notificationId,
                NotificationEnum.ORDER_CONFIRMED,
                "Подтверждение заказа",
                "Заказ подтвержден",
                NotificationStatus.SENT,
                now,
                false
        );

        NotificationListResponse notification2 = new NotificationListResponse(
                UUID.randomUUID(),
                NotificationEnum.ORDER_CANCELLED,
                "Отмена заказа",
                "Ваш заказ отменен",
                NotificationStatus.SENT,
                now,
                true
        );

        notificationListResponses = Arrays.asList(notification1, notification2);

        notificationDetailsResponse = new NotificationDetailsResponse(
                notificationId,
                NotificationEnum.ORDER_CONFIRMED,
                "Подтверждение заказа",
                "Ваш заказ был подтвержден. Спасибо за покупку!",
                NotificationStatus.SENT,
                now,
                now,
                false
        );
    }

    @Test
    void getUserNotifications_ShouldReturnOk() throws Exception {
        when(notificationService.getUserNotifications(userId)).thenReturn(notificationListResponses);
        
        mockMvc.perform(get("/api/v1/notifications/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(notificationId.toString()))
                .andExpect(jsonPath("$[0].type").value("ORDER_CONFIRMED"))
                .andExpect(jsonPath("$[0].title").value("Подтверждение заказа"))
                .andExpect(jsonPath("$[0].status").value("SENT"))
                .andExpect(jsonPath("$[0].isRead").value(false))
                .andExpect(jsonPath("$[1].type").value("ORDER_CANCELLED"))
                .andExpect(jsonPath("$[1].title").value("Отмена заказа"))
                .andExpect(jsonPath("$[1].isRead").value(true));

        verify(notificationService).getUserNotifications(userId);
    }

    @Test
    void getUserNotificationsByStatus_ShouldReturnOk() throws Exception {
        when(notificationService.getUserNotificationsByStatus(userId, NotificationStatus.SENT))
                .thenReturn(notificationListResponses);

        mockMvc.perform(get("/api/v1/notifications/user/{userId}/status/{status}", userId, NotificationStatus.SENT))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(notificationId.toString()))
                .andExpect(jsonPath("$[0].type").value("ORDER_CONFIRMED"))
                .andExpect(jsonPath("$[0].status").value("SENT"))
                .andExpect(jsonPath("$[1].type").value("ORDER_CANCELLED"))
                .andExpect(jsonPath("$[1].status").value("SENT"));

        verify(notificationService).getUserNotificationsByStatus(userId, NotificationStatus.SENT);
    }

    @Test
    void getNotificationDetails_ShouldReturnOk() throws Exception {
        when(notificationService.getNotificationDetails(notificationId)).thenReturn(notificationDetailsResponse);

        mockMvc.perform(get("/api/v1/notifications/{notificationId}", notificationId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(notificationId.toString()))
                .andExpect(jsonPath("$.type").value("ORDER_CONFIRMED"))
                .andExpect(jsonPath("$.title").value("Подтверждение заказа"))
                .andExpect(jsonPath("$.fullMessage").value("Ваш заказ был подтвержден. Спасибо за покупку!"))
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.isRead").value(false));

        verify(notificationService).getNotificationDetails(notificationId);
    }

    @Test
    void getNotificationDetails_WhenNotificationNotFound_ShouldReturnNotFound() throws Exception {
        when(notificationService.getNotificationDetails(notificationId))
                .thenThrow(new NotificationNotFoundException("Уведомление не найдено"));

        mockMvc.perform(get("/api/v1/notifications/{notificationId}", notificationId))
                .andExpect(status().isNotFound());

        verify(notificationService).getNotificationDetails(notificationId);
    }

    @Test
    void markNotificationAsRead_ShouldReturnOk() throws Exception {
        doNothing().when(notificationService).markNotificationAsRead(notificationId);

        mockMvc.perform(post("/api/v1/notifications/{notificationId}/read", notificationId))
                .andExpect(status().isOk());

        verify(notificationService).markNotificationAsRead(notificationId);
    }

    @Test
    void markNotificationAsRead_WhenNotificationNotFound_ShouldReturnNotFound() throws Exception {
        doThrow(new NotificationNotFoundException("Уведомление не найдено"))
                .when(notificationService).markNotificationAsRead(notificationId);
        
        mockMvc.perform(post("/api/v1/notifications/{notificationId}/read", notificationId))
                .andExpect(status().isNotFound());

        verify(notificationService).markNotificationAsRead(notificationId);
    }

    @Test
    void getUnreadNotificationsCount_ShouldReturnOk() throws Exception {
        long unreadCount = 5L;
        when(notificationService.countUnreadNotifications(userId)).thenReturn(unreadCount);
        
        mockMvc.perform(get("/api/v1/notifications/user/{userId}/unread/count", userId))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(unreadCount)));

        verify(notificationService).countUnreadNotifications(userId);
    }
}