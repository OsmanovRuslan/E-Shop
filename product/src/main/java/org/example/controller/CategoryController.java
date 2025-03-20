package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.data.dto.category.CategoryCreateRequest;
import org.example.data.dto.category.CategoryResponse;
import org.example.data.dto.category.CategoryUpdateRequest;
import org.example.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для управления категориями товаров.
 * Предоставляет API для создания, получения, обновления и удаления категорий.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Создание новой категории.
     * Доступно пользователям с ролями MANAGER или ADMIN.
     *
     * @param request Данные для создания категории
     * @return ResponseEntity с информацией о созданной категории
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryCreateRequest request) {
        log.info("Получен запрос создание категории с name: {}", request.name());
        CategoryResponse response = categoryService.createCategory(request);
        log.info("Создана категория с name: {} и id: {}", response.name(), response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Получение категории по ID.
     * Доступно всем пользователям без аутентификации.
     *
     * @param categoryId Уникальный идентификатор категории
     * @return ResponseEntity с информацией о категории
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable("id") UUID categoryId) {
        log.info("Получен запрос на получение категории с id: {}", categoryId);
        CategoryResponse response = categoryService.getCategoryById(categoryId);
        log.info("Получена категория с id: {}", categoryId);
        return ResponseEntity.ok(response);
    }

    /**
     * Получение списка всех категорий.
     * Доступно всем пользователям без аутентификации.
     *
     * @return ResponseEntity со списком всех категорий
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        log.info("Получен запрос на получение списка категорий");
        List<CategoryResponse> responses = categoryService.getAllCategories();
        log.info("Получен список категорий, количество: {}", responses.size());
        return ResponseEntity.ok(responses);
    }

    /**
     * Обновление категории.
     * Доступно пользователям с ролями MANAGER или ADMIN.
     *
     * @param categoryId Уникальный идентификатор категории
     * @param request Данные для обновления категории
     * @return ResponseEntity с обновленной информацией о категории
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable("id") UUID categoryId, @RequestBody CategoryUpdateRequest request) {
        log.info("Получен запрос на обновление категории с id: {}", categoryId);
        CategoryResponse response = categoryService.updateCategory(categoryId, request);
        log.info("Категория с id: {} успешно обновлена", categoryId);
        return ResponseEntity.ok(response);
    }

    /**
     * Удаление категории.
     * Доступно пользователям с ролями MANAGER или ADMIN.
     *
     * @param categoryId Уникальный идентификатор категории
     * @return ResponseEntity без тела ответа
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") UUID categoryId) {
        log.info("Получен запрос на удаление категории с id: {}", categoryId);
        categoryService.deleteCategory(categoryId);
        log.info("Категория с id: {} успешно удалена", categoryId);
        return ResponseEntity.noContent().build();
    }
}
