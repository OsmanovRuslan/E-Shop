package org.example.data.mapper;

import org.example.data.dto.category.CategoryCreateRequest;
import org.example.data.dto.category.CategoryResponse;
import org.example.data.dto.category.CategoryUpdateRequest;
import org.example.data.entity.CategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Интерфейс маппера для преобразования между DTO и сущностями категории.
 * Использует MapStruct для автоматической генерации реализации.
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    /**
     * Преобразование сущности категории в DTO ответа о категории.
     *
     * @param categoryEntity Сущность категории
     * @return DTO ответа о категории
     */
    CategoryResponse toCategoryDto(CategoryEntity categoryEntity);

    /**
     * Преобразование DTO запроса на создание категории в сущность категории.
     *
     * @param createRequest DTO запроса на создание категории
     * @return Сущность категории
     */
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    CategoryEntity toCategoryEntity(CategoryCreateRequest createRequest);

    /**
     * Обновление сущности категории из DTO запроса на обновление.
     *
     * @param updateRequest DTO запроса на обновление категории
     * @param categoryEntity Сущность категории для обновления
     */
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateCategoryFromDto(CategoryUpdateRequest updateRequest, @MappingTarget CategoryEntity categoryEntity);
}