package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.data.dto.product.ProductCreateRequest;
import org.example.data.dto.product.ProductResponse;
import org.example.data.dto.product.ProductSummaryResponse;
import org.example.data.dto.product.ProductUpdateRequest;
import org.example.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для управления товарами.
 * Предоставляет API для создания, получения, обновления и удаления товаров,
 * а также для поиска товаров по различным критериям.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    /**
     * Создание нового товара.
     * Доступно пользователям с ролями MANAGER или ADMIN.
     *
     * @param request Данные для создания товара
     * @return ResponseEntity с информацией о созданном товаре
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductCreateRequest request) {
        log.info("Получен запрос на создание товара с name: {}", request.name());
        ProductResponse response = productService.createProduct(request);
        log.info("Создан товар с id: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Получение товара по ID.
     * Доступно всем пользователям без аутентификации.
     *
     * @param productId Уникальный идентификатор товара
     * @return ResponseEntity с полной информацией о товаре
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("id") UUID productId) {
        log.info("Получен запрос на получение товара с id: {}", productId);
        ProductResponse response = productService.getProductById(productId);
        log.info("Получен товар с id: {}", productId);
        return ResponseEntity.ok(response);
    }

    /**
     * Получение списка товаров по их ID (batch запрос).
     * Используется другими микросервисами для эффективной загрузки данных о нескольких товарах одновременно.
     * Доступно всем пользователям без аутентификации.
     *
     * @param ids Список уникальных идентификаторов товаров
     * @return ResponseEntity со списком информации о товарах
     */
    @GetMapping("/batch")
    public ResponseEntity<List<ProductResponse>> getProductsByIds(@RequestParam List<UUID> ids) {
        log.info("Получен запрос на получение товаров по списку id, количество: {}", ids.size());
        List<ProductResponse> responses = productService.getProductsByIds(ids);
        log.info("Получен список товаров по ID, количество: {}", responses.size());
        return ResponseEntity.ok(responses);
    }

    /**
     * Получение списка всех товаров.
     * Доступно всем пользователям без аутентификации.
     *
     * @return ResponseEntity со списком краткой информации о всех товарах
     */
    @GetMapping
    public ResponseEntity<List<ProductSummaryResponse>> getAllProducts() {
        log.info("Получен запрос получение списка товаров");
        List<ProductSummaryResponse> responses = productService.getAllProducts();
        log.info("Получен список товаров, количество: {}", responses.size());
        return ResponseEntity.ok(responses);
    }

    /**
     * Получение списка товаров по категории.
     * Доступно всем пользователям без аутентификации.
     *
     * @param categoryId Уникальный идентификатор категории
     * @return ResponseEntity со списком краткой информации о товарах в категории
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductSummaryResponse>> getProductsByCategory(@PathVariable UUID categoryId) {
        log.info("Получен запрос на получение товаров по категории с id: {}", categoryId);
        List<ProductSummaryResponse> responses = productService.getProductsByCategory(categoryId);
        log.info("Получен список товаров по категории, количество: {}", responses.size());
        return ResponseEntity.ok(responses);
    }

    /**
     * Обновление товара.
     * Доступно пользователям с ролями MANAGER или ADMIN.
     *
     * @param productId Уникальный идентификатор товара
     * @param request Данные для обновления товара
     * @return ResponseEntity с обновленной информацией о товаре
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable("id") UUID productId, @RequestBody ProductUpdateRequest request) {
        log.info("Получен запрос на обновление товара с id: {}", productId);
        ProductResponse response = productService.updateProduct(productId, request);
        log.info("Товар с id: {} успешно обновлен", productId);
        return ResponseEntity.ok(response);
    }

    /**
     * Удаление товара.
     * Доступно пользователям с ролями MANAGER или ADMIN.
     *
     * @param productId Уникальный идентификатор товара
     * @return ResponseEntity без тела ответа
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") UUID productId) {
        log.info("Получен запрос на удаление товара с id: {}", productId);
        productService.deleteProduct(productId);
        log.info("Товар с id: {} успешно удален", productId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Поиск товаров по названию.
     * Доступно всем пользователям без аутентификации.
     *
     * @param query Поисковый запрос (часть названия товара)
     * @return ResponseEntity со списком краткой информации о найденных товарах
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductSummaryResponse>> searchProducts(@RequestParam String query) {
        log.info("Получен запрос на поиск товаров с запросом: {}", query);
        List<ProductSummaryResponse> responses = productService.searchProducts(query);
        log.info("Найдено товаров: {}", responses.size());
        return ResponseEntity.ok(responses);
    }

    /**
     * Установка активности товара.
     * Доступно пользователям с ролями MANAGER или ADMIN.
     *
     * @param productId Уникальный идентификатор товара
     * @param active Флаг активности товара
     * @return ResponseEntity с обновленной информацией о товаре
     */
    @PutMapping("/{id}/active")
    public ResponseEntity<ProductResponse> setProductActive(@PathVariable("id") UUID productId, @RequestParam boolean active) {
        log.info("Получен запрос на установку активности товара с id: {} в {}", productId, active);
        ProductResponse response = productService.setProductActive(productId, active);
        log.info("Активность товара с id: {} установлена в {}", productId, active);
        return ResponseEntity.ok(response);
    }
}
