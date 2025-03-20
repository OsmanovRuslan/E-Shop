package org.example.data.dto.image;

/**
 * DTO для запроса на создание изображения товара.
 *
 * @param imageUrl URL изображения
 * @param isPrimary Флаг, указывающий является ли изображение основным
 */
public record ProductImageCreateRequest (

        String imageUrl,

        Boolean isPrimary
){}