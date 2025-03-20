package org.example.data.dto.image;

import java.util.UUID;

/**
 * DTO для ответа с информацией об изображении товара.
 *
 * @param id Уникальный идентификатор изображения
 * @param imageUrl URL изображения
 * @param isPrimary Флаг, указывающий является ли изображение основным
 */
public record ProductImageResponse(

        UUID id,

        String imageUrl,

        Boolean isPrimary

) {}