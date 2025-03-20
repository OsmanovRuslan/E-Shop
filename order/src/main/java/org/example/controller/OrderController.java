package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.data.dto.order.AddToCartRequest;
import org.example.data.dto.order.OrderResponse;
import org.example.data.dto.order.OrderSummaryResponse;
import org.example.data.dto.order.UpdateQuantityRequest;
import org.example.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для обработки API-запросов, связанных с заказами и корзинами пользователей.
 * Предоставляет эндпоинты для создания, обновления и управления заказами.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * Получение заказа по ID.
     * Доступно только аутентифицированным пользователям.
     *
     * @param orderId Уникальный идентификатор заказа
     * @return ResponseEntity с данными заказа
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID orderId) {
        log.info("Получен запрос на получение заказа с id: {}", orderId);
        OrderResponse response = orderService.getOrderById(orderId);
        log.info("Получен заказ с id: {}", orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Получение всех заказов пользователя.
     * Доступно только аутентифицированным пользователям.
     *
     * @param userId Уникальный идентификатор пользователя
     * @return ResponseEntity со списком сводной информации о заказах пользователя
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderSummaryResponse>> getOrdersByUserId(@PathVariable UUID userId) {
        log.info("Получен запрос на получение всех заказов пользователя с id: {}", userId);
        List<OrderSummaryResponse> responses = orderService.getOrdersByUserId(userId);
        log.info("Получены заказы пользователя с id: {}, количество: {}", userId, responses.size());
        return ResponseEntity.ok(responses);
    }

    /**
     * Получение корзины пользователя.
     * Доступно только аутентифицированным пользователям.
     *
     * @param userId Уникальный идентификатор пользователя
     * @return ResponseEntity с данными текущей корзины пользователя
     */
    @GetMapping("/user/{userId}/cart")
    public ResponseEntity<OrderResponse> getCartByUserId(@PathVariable UUID userId) {
        log.info("Получен запрос на получение корзины пользователя с id: {}", userId);
        OrderResponse response = orderService.getCartByUserId(userId);
        log.info("Получена корзина пользователя с id: {}", userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Добавление товаров в корзину.
     * Доступно только аутентифицированным пользователям.
     *
     * @param userId Уникальный идентификатор пользователя
     * @param request Запрос с данными о товарах для добавления в корзину
     * @return ResponseEntity с обновленными данными корзины
     */
    @PostMapping("/user/{userId}/cart")
    public ResponseEntity<OrderResponse> addToCart(@PathVariable UUID userId, @RequestBody AddToCartRequest request) {
        log.info("Получен запрос на добавление товаров в корзину пользователя с id: {}", userId);
        OrderResponse response = orderService.addToCart(userId, request);
        log.info("Товары добавлены в корзину пользователя с id: {}", userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Удаление товара из корзины.
     * Доступно только аутентифицированным пользователям.
     *
     * @param orderId Уникальный идентификатор заказа (корзины)
     * @param productId Уникальный идентификатор товара для удаления
     * @return ResponseEntity с обновленными данными корзины
     */
    @DeleteMapping("/{orderId}/items/{productId}")
    public ResponseEntity<OrderResponse> removeFromCart(@PathVariable UUID orderId, @PathVariable UUID productId) {
        log.info("Получен запрос на удаление товара с id: {} из заказа с id: {}", productId, orderId);
        OrderResponse response = orderService.removeFromCart(orderId, productId);
        log.info("Товар с id: {} удален из заказа с id: {}", productId, orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Обновление количества товара в корзине.
     * Доступно только аутентифицированным пользователям.
     *
     * @param orderId Уникальный идентификатор заказа (корзины)
     * @param productId Уникальный идентификатор товара для обновления
     * @param request Запрос с новым количеством товара
     * @return ResponseEntity с обновленными данными корзины
     */
    @PutMapping("/{orderId}/items/{productId}")
    public ResponseEntity<OrderResponse> updateItemQuantity(@PathVariable UUID orderId, @PathVariable UUID productId, @RequestBody UpdateQuantityRequest request) {
        log.info("Получен запрос на обновление количества товара с id: {} в заказе с id: {}", productId, orderId);
        OrderResponse response = orderService.updateItemQuantity(orderId, productId, request.quantity());
        log.info("Обновлено количество товара с id: {} в заказе с id: {}", productId, orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Очистка корзины.
     * Доступно только аутентифицированным пользователям.
     *
     * @param orderId Уникальный идентификатор заказа (корзины)
     * @return ResponseEntity с обновленными данными корзины
     */
    @DeleteMapping("/{orderId}/items")
    public ResponseEntity<OrderResponse> clearCart(@PathVariable UUID orderId) {
        log.info("Получен запрос на очистку заказа с id: {}", orderId);
        OrderResponse response = orderService.clearCart(orderId);
        log.info("Заказ с id: {} очищен", orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Подтверждение заказа.
     * Доступно только аутентифицированным пользователям.
     *
     * @param orderId Уникальный идентификатор заказа
     * @return ResponseEntity с обновленными данными заказа
     */
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable UUID orderId) {
        log.info("Получен запрос на подтверждение заказа с id: {}", orderId);
        OrderResponse response = orderService.confirmOrder(orderId);
        log.info("Заказ с id: {} подтвержден", orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Отмена заказа.
     * Доступно только аутентифицированным пользователям.
     *
     * @param orderId Уникальный идентификатор заказа
     * @return ResponseEntity с обновленными данными заказа
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID orderId) {
        log.info("Получен запрос на отмену заказа с id: {}", orderId);
        OrderResponse response = orderService.cancelOrder(orderId);
        log.info("Заказ с id: {} отменен", orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Обновление адреса доставки для заказа.
     * Доступно только аутентифицированным пользователям.
     *
     * @param orderId Уникальный идентификатор заказа
     * @param addressId Уникальный идентификатор нового адреса доставки
     * @return ResponseEntity с обновленными данными заказа
     */
    @PutMapping("/{orderId}/address/{addressId}")
    public ResponseEntity<OrderResponse> updateAddress(@PathVariable UUID orderId, @PathVariable UUID addressId) {
        log.info("Получен запрос на обновление адреса заказа с id: {} на адрес с id: {}", orderId, addressId);
        OrderResponse response = orderService.updateAddress(orderId, addressId);
        log.info("Обновлен адрес заказа с id: {}", orderId);
        return ResponseEntity.ok(response);
    }

}