package org.example.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.data.dto.image.ProductImageCreateRequest;
import org.example.data.dto.image.ProductImageResponse;
import org.example.data.entity.ProductEntity;
import org.example.data.entity.ProductImageEntity;
import org.example.exception.image.ProductImageNotFoundException;
import org.example.exception.product.ProductNotFoundException;
import org.example.data.mapper.ProductImageMapper;
import org.example.repository.ProductImageRepository;
import org.example.repository.ProductRepository;
import org.example.service.ProductImageService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final ProductImageMapper productImageMapper;

    @Override
    public ProductImageResponse getImageById(UUID imageId) {
        log.debug("Получение изображения с id: {}", imageId);

        ProductImageEntity imageEntity = getImageOrThrow(imageId);

        log.debug("Получено изображение с id: {}", imageId);
        return productImageMapper.toProductImageDto(imageEntity);
    }

    @Override
    public ProductImageResponse addImageToProduct(UUID productId, ProductImageCreateRequest request) {
        log.debug("Добавление изображения к товару с id: {}", productId);

        ProductEntity productEntity = getProductOrThrow(productId);

        ProductImageEntity imageEntity = productImageMapper.toProductImageEntity(request);
        imageEntity.setProduct(productEntity);

        if (request.isPrimary() || productEntity.getImages().isEmpty()) {
            if (request.isPrimary()) {
                productEntity.getImages().stream()
                        .filter(ProductImageEntity::getIsPrimary)
                        .forEach(img -> img.setIsPrimary(false));
            }

            imageEntity.setIsPrimary(true);
        }

        productEntity.getImages().add(imageEntity);
        productRepository.save(productEntity);

        log.debug("Добавлено изображение к товару с id: {}", productId);
        return productImageMapper.toProductImageDto(imageEntity);
    }

    @Override
    public List<ProductImageResponse> getProductImages(UUID productId) {
        log.debug("Получение изображений товара с id: {}", productId);

        ProductEntity productEntity = getProductOrThrow(productId);

        List<ProductImageResponse> images = productEntity.getImages().stream()
                .map(productImageMapper::toProductImageDto)
                .toList();

        log.debug("Получено {} изображений товара с id: {}", images.size(), productId);
        return images;
    }

    @Override
    public void removeImageFromProduct(UUID productId, UUID imageId) {
        log.debug("Удаление изображения с id: {} из товара с id: {}", imageId, productId);

        ProductEntity productEntity = getProductOrThrow(productId);

        boolean removed = productEntity.getImages().removeIf(image -> image.getId().equals(imageId));

        if (!removed) {
            log.error("Изображение с id: {} не найдено у товара с id: {}", imageId, productId);
            throw new ProductImageNotFoundException(String.format("Изображение с id: %s не найдено у товара с id: %s", imageId, productId));
        }

        if (productEntity.getImages().stream().noneMatch(ProductImageEntity::getIsPrimary) &&
                !productEntity.getImages().isEmpty()) {
            productEntity.getImages().iterator().next().setIsPrimary(true);
        }

        productRepository.save(productEntity);

        productImageRepository.deleteById(imageId);

        log.debug("Удалено изображение с id: {} из товара с id: {}", imageId, productId);
    }

    @Override
    public ProductImageResponse setPrimaryImage(UUID productId, UUID imageId) {
        log.debug("Установка изображения с id: {} как основного для товара с id: {}", imageId, productId);

        ProductEntity productEntity = getProductOrThrow(productId);

        ProductImageEntity newPrimaryImage = productEntity.getImages().stream()
                .filter(image -> image.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Изображение с id: {} не найдено у товара с id: {}", imageId, productId);
                    return new ProductImageNotFoundException(
                            String.format("Изображение с id: %s не найдено у товара с id: %s", imageId, productId));
                });

        productEntity.getImages().stream()
                .filter(image -> !image.getId().equals(imageId) && image.getIsPrimary())
                .forEach(image -> image.setIsPrimary(false));

        newPrimaryImage.setIsPrimary(true);

        productRepository.save(productEntity);

        log.debug("Установлено изображение с id: {} как основное для товара с id: {}", imageId, productId);
        return productImageMapper.toProductImageDto(newPrimaryImage);
    }

    private ProductEntity getProductOrThrow(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Товар с id: {} не найден", productId);
                    return new ProductNotFoundException(String.format("Товар с id: %s не найден", productId));
                });
    }

    private ProductImageEntity getImageOrThrow(UUID imageId) {
        return productImageRepository.findById(imageId)
                .orElseThrow(() -> {
                    log.error("Изображение с id: {} не найдено", imageId);
                    return new ProductImageNotFoundException(String.format("Изображение с id: %s не найдено", imageId));
                });
    }

}