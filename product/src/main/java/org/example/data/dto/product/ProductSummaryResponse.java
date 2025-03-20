package org.example.data.dto.product;

import org.example.data.dto.image.ProductImageResponse;

import java.util.UUID;

/**
 * DTO для ответа с краткой информацией о товаре.
 * Используется для списков товаров.
 *
 * @param id Уникальный идентификатор товара
 * @param name Название товара
 * @param price Цена товара
 * @param isActive Флаг активности товара
 * @param primaryImage Основное изображение товара
 */
public record ProductSummaryResponse (

        UUID id,

        String name,

        Double price,

        Boolean isActive,

        ProductImageResponse primaryImage

) {}
