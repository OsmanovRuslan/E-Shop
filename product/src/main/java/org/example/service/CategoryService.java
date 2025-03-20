package org.example.service;

import org.example.data.dto.category.CategoryCreateRequest;
import org.example.data.dto.category.CategoryResponse;
import org.example.data.dto.category.CategoryUpdateRequest;
import org.example.exception.NotFoundException;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс сервиса для управления категориями товаров.
 * Предоставляет методы для создания, получения, обновления и удаления категорий.
 */
public interface CategoryService {

    /**
     * Создание новой категории.
     * Доступно пользователям с ролями MANAGER или ADMIN.
     *
     * @param request данные для создания категории
     * @return информация о созданной категории
     * @throws IllegalArgumentException если запрос содержит некорректные данные
     */
    CategoryResponse createCategory(CategoryCreateRequest request);

    /**
     * Получение категории по ID.
     * Доступно всем пользователям без аутентификации.
     *
     * @param categoryId ID категории
     * @return информация о категории
     * @throws NotFoundException если категория с указанным ID не найдена
     */
    CategoryResponse getCategoryById(UUID categoryId);

    /**
     * Получение списка всех категорий.
     * Доступно всем пользователям без аутентификации.
     *
     * @return список категорий
     */
    List<CategoryResponse> getAllCategories();

    /**
     * Обновление категории.
     * Доступно пользователям с ролями MANAGER или ADMIN.
     *
     * @param categoryId ID категории
     * @param request данные для обновления
     * @return обновленная информация о категории
     * @throws NotFoundException если категория с указанным ID не найдена
     * @throws IllegalArgumentException если запрос содержит некорректные данные
     */
    CategoryResponse updateCategory(UUID categoryId, CategoryUpdateRequest request);

    /**
     * Удаление категории.
     * Доступно пользователям с ролями MANAGER или ADMIN.
     *
     * @param categoryId ID категории
     * @throws NotFoundException если категория с указанным ID не найдена
     */
    void deleteCategory(UUID categoryId);

}