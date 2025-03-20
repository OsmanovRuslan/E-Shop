package org.example.service;


import org.example.data.dto.image.ProductImageCreateRequest;
import org.example.data.dto.image.ProductImageResponse;
import org.example.exception.NotFoundException;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс сервиса для управления изображениями товаров.
 * Предоставляет методы для добавления, получения, обновления и удаления изображений товаров.
 */
public interface ProductImageService {

    /**
     * Получение изображения по ID.
     * Доступно всем пользователям без аутентификации.
     *
     * @param imageId ID изображения
     * @return информация об изображении
     * @throws NotFoundException если изображение с указанным ID не найдено
     */
    ProductImageResponse getImageById(UUID imageId);

    /**
     * Добавление нового изображения к товару.
     * Доступно пользователям с ролями MANAGER или ADMIN.
     *
     * @param productId ID товара
     * @param request данные изображения
     * @return информация о созданном изображении
     * @throws NotFoundException если товар с указанным ID не найден
     * @throws IllegalArgumentException если запрос содержит некорректные данные
     */
    ProductImageResponse addImageToProduct(UUID productId, ProductImageCreateRequest request);

    /**
     * Получение всех изображений товара.
     * Доступно всем пользователям без аутентификации.
     *
     * @param productId ID товара
     * @return список изображений товара
     * @throws NotFoundException если товар с указанным ID не найден
     */
    List<ProductImageResponse> getProductImages(UUID productId);

    /**
     * Удаление изображения из товара.
     * Доступно пользователям с ролями MANAGER или ADMIN.
     *
     * @param productId ID товара
     * @param imageId ID изображения
     * @throws NotFoundException если товар или изображение с указанным ID не найдены
     */
    void removeImageFromProduct(UUID productId, UUID imageId);

    /**
     * Установка изображения как основного для товара.
     * Основное изображение товара используется в качестве превью в списках товаров.
     * Доступно пользователям с ролями MANAGER или ADMIN.
     *
     * @param productId ID товара
     * @param imageId ID изображения
     * @return обновленная информация об изображении
     * @throws NotFoundException если товар или изображение с указанным ID не найдены
     */
    ProductImageResponse setPrimaryImage(UUID productId, UUID imageId);
}
