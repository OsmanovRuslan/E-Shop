package org.example.data.dto.product;

import org.example.data.dto.category.CategoryResponse;
import org.example.data.dto.image.ProductImageResponse;

import java.util.List;
import java.util.UUID;

/**
 * DTO для ответа с полной информацией о товаре.
 *
 * @param id Уникальный идентификатор товара
 * @param name Название товара
 * @param description Описание товара
 * @param price Цена товара
 * @param isActive Флаг активности товара
 * @param categories Список категорий товара
 * @param images Список всех изображений товара
 * @param primaryImage Основное изображение товара
 */
public record ProductResponse (

        UUID id,

        String name,

        String description,

        Double price,

        Boolean isActive,

        List<CategoryResponse> categories,

        List<ProductImageResponse> images,

        ProductImageResponse primaryImage

) {}
