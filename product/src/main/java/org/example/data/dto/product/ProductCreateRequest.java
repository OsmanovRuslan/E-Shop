package org.example.data.dto.product;

import org.example.data.dto.image.ProductImageCreateRequest;

import java.util.List;
import java.util.UUID;

/**
 * DTO для запроса на создание товара.
 *
 * @param name Название товара
 * @param description Описание товара
 * @param price Цена товара
 * @param createdBy Уникальный идентификатор пользователя, создавшего товар
 * @param isActive Флаг активности товара
 * @param categoryIds Список идентификаторов категорий
 * @param images Список изображений товара
 */
public record ProductCreateRequest (

        String name,

        String description,

        Double price,

        UUID createdBy,

        Boolean isActive,

        List<UUID> categoryIds,

        List<ProductImageCreateRequest> images

){}
