package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.data.dto.image.ProductImageCreateRequest;
import org.example.data.dto.image.ProductImageResponse;
import org.example.service.ProductImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для управления изображениями товаров.
 * Предоставляет API для добавления, получения, обновления и удаления изображений товаров.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products/{productId}/images")
public class ProductImageController {

    private final ProductImageService productImageService;

    /**
     * Добавление изображения к товару.
     * Доступно пользователям с ролями MANAGER или ADMIN.
     *
     * @param productId Уникальный идентификатор товара
     * @param request Данные для создания изображения
     * @return ResponseEntity с информацией о созданном изображении
     */
    @PostMapping
    public ResponseEntity<ProductImageResponse> addImageToProduct(@PathVariable UUID productId, @RequestBody ProductImageCreateRequest request) {
        log.info("Получен запрос на добавление изображения к товару с id: {}", productId);
        ProductImageResponse response = productImageService.addImageToProduct(productId, request);
        log.info("Добавлено изображение с id: {} к товару с id: {}", response.id(), productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Получение всех изображений товара.
     * Доступно всем пользователям без аутентификации.
     *
     * @param productId Уникальный идентификатор товара
     * @return ResponseEntity со списком изображений товара
     */
    @GetMapping
    public ResponseEntity<List<ProductImageResponse>> getProductImages(@PathVariable UUID productId) {
        log.info("Получен запрос на получение изображений товара с id: {}", productId);
        List<ProductImageResponse> responses = productImageService.getProductImages(productId);
        log.info("Получены изображения товара с id: {}, количество: {}", productId, responses.size());
        return ResponseEntity.ok(responses);
    }

    /**
     * Получение изображения по ID.
     * Доступно всем пользователям без аутентификации.
     *
     * @param productId Уникальный идентификатор товара
     * @param imageId Уникальный идентификатор изображения
     * @return ResponseEntity с информацией об изображении
     */
    @GetMapping("/{imageId}")
    public ResponseEntity<ProductImageResponse> getImageById(@PathVariable UUID productId, @PathVariable UUID imageId) {
        log.info("Получен запрос на получение изображения с id: {} товара с id: {}", imageId, productId);
        ProductImageResponse response = productImageService.getImageById(imageId);
        log.info("Получено изображение с id: {}", imageId);
        return ResponseEntity.ok(response);
    }

    /**
     * Установка изображения как основного для товара.
     * Доступно пользователям с ролями MANAGER или ADMIN.
     *
     * @param productId Уникальный идентификатор товара
     * @param imageId Уникальный идентификатор изображения
     * @return ResponseEntity с обновленной информацией об изображении
     */
    @PutMapping("/{imageId}/primary")
    public ResponseEntity<ProductImageResponse> setPrimaryImage(@PathVariable UUID productId, @PathVariable UUID imageId) {
        log.info("Получен запрос на установку изображения с id: {} как основного для товара с id: {}", imageId, productId);
        ProductImageResponse response = productImageService.setPrimaryImage(productId, imageId);
        log.info("Изображение с id: {} установлено как основное для товара с id: {}", imageId, productId);
        return ResponseEntity.ok(response);
    }

    /**
     * Удаление изображения товара.
     * Доступно пользователям с ролями MANAGER или ADMIN.
     *
     * @param productId Уникальный идентификатор товара
     * @param imageId Уникальный идентификатор изображения
     * @return ResponseEntity без тела ответа
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> removeImageFromProduct(@PathVariable UUID productId, @PathVariable UUID imageId) {
        log.info("Получен запрос на удаление изображения с id: {} из товара с id: {}", imageId, productId);
        productImageService.removeImageFromProduct(productId, imageId);
        log.info("Изображение с id: {} удалено из товара с id: {}", imageId, productId);
        return ResponseEntity.noContent().build();
    }
}