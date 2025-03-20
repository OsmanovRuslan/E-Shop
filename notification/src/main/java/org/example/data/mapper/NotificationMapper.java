package org.example.data.mapper;

import org.example.data.dto.NotificationDetailsResponse;
import org.example.data.dto.NotificationListResponse;
import org.example.data.entity.NotificationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Интерфейс для маппинга между сущностями уведомлений и DTO.
 * Использует MapStruct для автоматической генерации реализации.
 */
@Mapper(componentModel = "spring")
public interface NotificationMapper {

    /**
     * Преобразование сущности в DTO для списка уведомлений.
     *
     * @param entity Сущность уведомления
     * @return DTO для отображения в списке уведомлений
     */
    @Mapping(target = "preview", expression = "java(createPreview(entity.getMessage()))")
    @Mapping(target = "updatedAt", expression = "java(entity.getUpdatedAt())")
    NotificationListResponse toNotificationListResponse(NotificationEntity entity);

    /**
     * Преобразование списка сущностей в список DTO для списка уведомлений.
     *
     * @param entities Список сущностей уведомлений
     * @return Список DTO для отображения в списке уведомлений
     */
    List<NotificationListResponse> toNotificationListResponseList(List<NotificationEntity> entities);

    /**
     * Преобразование сущности в DTO с детальной информацией об уведомлении.
     *
     * @param entity Сущность уведомления
     * @return DTO с детальной информацией об уведомлении
     */
    @Mapping(target = "fullMessage", source = "message")
    @Mapping(target = "createdAt", expression = "java(entity.getCreatedAt())")
    @Mapping(target = "updatedAt", expression = "java(entity.getUpdatedAt())")
    NotificationDetailsResponse toNotificationDetailsResponse(NotificationEntity entity);

    /**
     * Вспомогательный метод для создания превью сообщения (первые 100 символов).
     *
     * @param message Полный текст сообщения
     * @return Сокращенный текст сообщения для превью
     */
    default String createPreview(String message) {
        if (message == null) {
            return null;
        }
        if (message.length() <= 100) {
            return message;
        }
        return message.substring(0, 97) + "...";
    }

}