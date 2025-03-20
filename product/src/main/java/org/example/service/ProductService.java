package org.example.service;

import org.example.data.dto.product.ProductCreateRequest;
import org.example.data.dto.product.ProductResponse;
import org.example.data.dto.product.ProductSummaryResponse;
import org.example.data.dto.product.ProductUpdateRequest;
import org.example.exception.NotFoundException;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс сервиса для управления товарами.
 * Предоставляет методы для создания, получения, обновления и удаления товаров,
 * а также для поиска и фильтрации товаров по различным критериям.
 */
public interface ProductService {

    /**
     * Создание нового товара.
     * Доступно пользователям с ролями MANAGER или ADMIN.
     *
     * @param request данные для создания товара
     * @return информация о созданном товаре
     * @throws IllegalArgumentException если запрос содержит некорректные данные
     */
    ProductResponse createProduct(ProductCreateRequest request);

    /**
     * Получение товара по ID.
     * Доступно всем пользователям без аутентификации.
     *
     * @param productId ID товара
     * @return детальная информация о товаре
     * @throws NotFoundException если товар с указанным ID не найден
     */
    ProductResponse getProductById(UUID productId);

    /**
     * Получение списка товаров по их ID.
     * Метод используется для эффективной загрузки данных о нескольких товарах одновременно.
     * Доступно всем пользователям без аутентификации.
     *
     * @param productIds список ID товаров
     * @return список детальной информации о товарах
     */
    List<ProductResponse> getProductsByIds(List<UUID> productIds);

    /**
     * Получение списка всех товаров.
     * Доступно всем пользователям без аутентификации.
     *
     * @return список сокращенной информации о товарах
     */
    List<ProductSummaryResponse> getAllProducts();

    /**
     * Получение списка товаров по категории.
     * Доступно всем пользователям без аутентификации.
     *
     * @param categoryId ID категории
     * @return список сокращенной информации о товарах
     * @throws NotFoundException если категория с указанным ID не найдена
     */
    List<ProductSummaryResponse> getProductsByCategory(UUID categoryId);

    /**
     * Обновление товара.
     * Доступно пользователям с ролями MANAGER или ADMIN.
     *
     * @param productId ID товара
     * @param request данные для обновления
     * @return обновленная информация о товаре
     * @throws NotFoundException если товар с указанным ID не найден
     * @throws IllegalArgumentException если запрос содержит некорректные данные
     */
    ProductResponse updateProduct(UUID productId, ProductUpdateRequest request);

    /**
     * Удаление товара.
     * Доступно пользователям с ролями MANAGER или ADMIN.
     *
     * @param productId ID товара
     * @throws NotFoundException если товар с указанным ID не найден
     */
    void deleteProduct(UUID productId);

    /**
     * Поиск товаров по названию или описанию.
     * Доступно всем пользователям без аутентификации.
     *
     * @param query поисковый запрос
     * @return список найденных товаров
     */
    List<ProductSummaryResponse> searchProducts(String query);

    /**
     * Установка активности товара.
     * Активный товар отображается в каталоге и доступен для заказа,
     * неактивный товар скрыт от пользователей.
     * Доступно пользователям с ролями MANAGER или ADMIN.
     *
     * @param productId ID товара
     * @param active статус активности
     * @return обновленная информация о товаре
     * @throws NotFoundException если товар с указанным ID не найден
     */
    ProductResponse setProductActive(UUID productId, boolean active);

}