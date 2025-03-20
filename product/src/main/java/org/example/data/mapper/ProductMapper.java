package org.example.data.mapper;

import org.example.data.dto.image.ProductImageResponse;
import org.example.data.dto.product.ProductCreateRequest;
import org.example.data.dto.product.ProductResponse;
import org.example.data.dto.product.ProductSummaryResponse;
import org.example.data.dto.product.ProductUpdateRequest;
import org.example.data.entity.ProductEntity;
import org.example.data.entity.ProductImageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Set;

/**
 * Интерфейс маппера для преобразования между DTO и сущностями товара.
 * Использует MapStruct для автоматической генерации реализации.
 */
@Mapper(componentModel = "spring", uses = {CategoryMapper.class, ProductImageMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

    /**
     * Преобразование DTO запроса на создание товара в сущность товара.
     *
     * @param createRequest DTO запроса на создание товара
     * @return Сущность товара
     */
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "categories", ignore = true)
    ProductEntity toProductEntity(ProductCreateRequest createRequest);

    /**
     * Преобразование сущности товара в DTO полного ответа о товаре.
     *
     * @param productEntity Сущность товара
     * @return DTO полного ответа о товаре
     */
    @Mapping(target = "primaryImage", ignore = true)
    ProductResponse toProductResponse(ProductEntity productEntity);

    /**
     * Преобразование сущности товара в DTO краткого ответа о товаре.
     * Включает поиск основного изображения товара.
     *
     * @param productEntity Сущность товара
     * @return DTO краткого ответа о товаре
     */
    @Mapping(target = "primaryImage", expression = "java(findPrimaryImage(productEntity.getImages()))")
    ProductSummaryResponse toProductSummaryResponse(ProductEntity productEntity);

    /**
     * Обновление сущности товара из DTO запроса на обновление.
     *
     * @param updateRequest DTO запроса на обновление товара
     * @param productEntity Сущность товара для обновления
     */
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "categories", ignore = true)
    void updateProductFromDto(ProductUpdateRequest updateRequest, @MappingTarget ProductEntity productEntity);

    /**
     * Поиск основного изображения товара среди набора изображений.
     * Если основное изображение не найдено, возвращает первое изображение из набора.
     *
     * @param images Набор изображений товара
     * @return DTO ответа об изображении товара или null, если изображений нет
     */
    default ProductImageResponse findPrimaryImage(Set<ProductImageEntity> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }

        return images.stream()
                .filter(ProductImageEntity::getIsPrimary)
                .findFirst()
                .map(image -> new ProductImageResponse(image.getId(), image.getImageUrl(), image.getIsPrimary()))
                .orElseGet(() -> {
                    ProductImageEntity first = images.stream().findFirst().get();
                    return new ProductImageResponse(first.getId(), first.getImageUrl(), first.getIsPrimary());
                });
    }
}
