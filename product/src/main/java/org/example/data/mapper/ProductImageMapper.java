package org.example.data.mapper;

import org.example.data.dto.image.ProductImageCreateRequest;
import org.example.data.dto.image.ProductImageResponse;
import org.example.data.entity.ProductImageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Интерфейс маппера для преобразования между DTO и сущностями изображения товара.
 * Использует MapStruct для автоматической генерации реализации.
 */
@Mapper(componentModel = "spring")
public interface ProductImageMapper {

    /**
     * Преобразование DTO запроса на создание изображения товара в сущность изображения товара.
     *
     * @param createRequest DTO запроса на создание изображения товара
     * @return Сущность изображения товара
     */
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ProductImageEntity toProductImageEntity(ProductImageCreateRequest createRequest);

    /**
     * Преобразование сущности изображения товара в DTO ответа об изображении товара.
     *
     * @param image Сущность изображения товара
     * @return DTO ответа об изображении товара
     */
    ProductImageResponse toProductImageDto(ProductImageEntity image);
}
