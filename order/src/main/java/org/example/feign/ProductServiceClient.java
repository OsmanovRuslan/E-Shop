package org.example.feign;

import feign.FeignException;
import org.example.data.dto.feign.product.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

/**
 * Клиент для взаимодействия с Product Service через Feign.
 * Позволяет получать информацию о продуктах.
 */
@FeignClient(name = "product-service")
public interface ProductServiceClient {

    /**
     * Получение товара по ID.
     *
     * @param productId Уникальный идентификатор товара
     * @return ResponseEntity с информацией о товаре
     * @throws FeignException если произошла ошибка при вызове Product Service
     */
    @GetMapping("/api/v1/products/{productId}")
    ResponseEntity<ProductDto> getProductById(@PathVariable UUID productId);


    /**
     * Получение списка товаров по их ID.
     *
     * @param productIds Список уникальных идентификаторов товаров
     * @return ResponseEntity со списком информации о товарах
     * @throws FeignException если произошла ошибка при вызове Product Service
     */
    @GetMapping("/api/v1/products/batch")
    ResponseEntity<List<ProductDto>> getProductsByIds(@RequestParam List<UUID> productIds);
}
