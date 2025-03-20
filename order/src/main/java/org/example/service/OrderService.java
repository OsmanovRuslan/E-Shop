package org.example.service;

import org.example.data.dto.order.AddToCartRequest;
import org.example.data.dto.order.OrderResponse;
import org.example.data.dto.order.OrderSummaryResponse;
import org.example.exception.feign.DataGettingException;
import org.example.exception.feign.user.UserNotFoundException;
import org.example.exception.order.InvalidOrderOperationException;
import org.example.exception.order.OrderNotFoundException;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс сервиса заказов.
 * Определяет методы для работы с заказами и корзинами пользователей.
 */
public interface OrderService {

    /**
     * Добавление товаров в корзину. Если у пользователя нет активной корзины, создает новую.
     *
     * @param userId ID пользователя
     * @param request данные о товарах для добавления
     * @return обновленные данные корзины
     * @throws UserNotFoundException если пользователь не найден
     * @throws InvalidOrderOperationException если выбранный адрес не принадлежит пользователю
     */
    OrderResponse addToCart(UUID userId, AddToCartRequest request);

    /**
     * Удаление товара из корзины.
     *
     * @param orderId ID заказа
     * @param productId ID товара
     * @return обновленные данные корзины
     * @throws OrderNotFoundException если заказ не найден
     * @throws InvalidOrderOperationException если заказ не в статусе корзины или товар не найден
     */
    OrderResponse removeFromCart(UUID orderId, UUID productId);

    /**
     * Обновление количества товара в корзине.
     *
     * @param orderId ID заказа
     * @param productId ID товара
     * @param quantity новое количество
     * @return обновленные данные корзины
     * @throws OrderNotFoundException если заказ не найден
     * @throws InvalidOrderOperationException если заказ не в статусе корзины или товар не найден
     */
    OrderResponse updateItemQuantity(UUID orderId, UUID productId, Integer quantity);

    /**
     * Очистка корзины (удаление всех товаров).
     *
     * @param orderId ID заказа
     * @return пустая корзина
     * @throws OrderNotFoundException если заказ не найден
     * @throws InvalidOrderOperationException если заказ не в статусе корзины
     */
    OrderResponse clearCart(UUID orderId);

    /**
     * Подтверждение заказа (перевод из статуса CART в CONFIRMED).
     *
     * @param orderId ID заказа
     * @return подтвержденный заказ
     * @throws OrderNotFoundException если заказ не найден
     * @throws InvalidOrderOperationException если заказ не в статусе корзины или корзина пуста
     * @throws DataGettingException если произошла ошибка при получении данных от других сервисов
     */
    OrderResponse confirmOrder(UUID orderId);

    /**
     * Отмена заказа.
     *
     * @param orderId ID заказа
     * @return отмененный заказ
     * @throws OrderNotFoundException если заказ не найден
     * @throws InvalidOrderOperationException если заказ уже отменен или находится в статусе корзины
     * @throws DataGettingException если произошла ошибка при получении данных от других сервисов
     */
    OrderResponse cancelOrder(UUID orderId);

    /**
     * Получение заказа по ID.
     *
     * @param orderId ID заказа
     * @return данные заказа
     * @throws OrderNotFoundException если заказ не найден
     */
    OrderResponse getOrderById(UUID orderId);

    /**
     * Получение активной корзины пользователя.
     *
     * @param userId ID пользователя
     * @return данные корзины
     * @throws OrderNotFoundException если корзина не найдена
     */
    OrderResponse getCartByUserId(UUID userId);

    /**
     * Получение всех заказов пользователя.
     *
     * @param userId ID пользователя
     * @return список заказов
     */
    List<OrderSummaryResponse> getOrdersByUserId(UUID userId);

    /**
     * Обновление адреса доставки.
     *
     * @param orderId ID заказа
     * @param addressId ID адреса
     * @return обновленный заказ
     * @throws OrderNotFoundException если заказ не найден
     * @throws InvalidOrderOperationException если выбранный адрес не принадлежит пользователю
     */
    OrderResponse updateAddress(UUID orderId, UUID addressId);
}
