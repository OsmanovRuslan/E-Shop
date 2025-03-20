package org.example.data.dto.product;

import org.example.data.dto.image.ProductImageCreateRequest;

import java.util.List;
import java.util.UUID;

/**
 * DTO для запроса на обновление товара.
 *
 * @param name Новое название товара
 * @param description Новое описание товара
 * @param price Новая цена товара
 * @param isActive Новый флаг активности товара
 * @param categoryIds Новый список идентификаторов категорий
 * @param images Новый список изображений товара
 */
public record ProductUpdateRequest (

        String name,

        String description,

        Double price,

        Boolean isActive,

        List<UUID> categoryIds,

        List<ProductImageCreateRequest> images

){}
