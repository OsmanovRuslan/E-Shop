package org.example.service.impl;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.data.dto.feign.product.ProductDto;
import org.example.data.dto.feign.user.AddressResponse;
import org.example.data.dto.feign.user.UserDetailResponse;
import org.example.data.dto.order.*;
import org.example.data.entity.OrderEntity;
import org.example.data.entity.OrderItemEntity;
import org.example.data.event.type.OrderCancelEvent;
import org.example.data.event.type.OrderConfirmEvent;
import org.example.data.event.OrderEvent;
import org.example.data.event.extra.OrderItem;
import org.example.exception.feign.DataGettingException;
import org.example.exception.feign.address.AddressNotFoundException;
import org.example.exception.order.InvalidOrderOperationException;
import org.example.exception.order.OrderNotFoundException;
import org.example.exception.feign.user.UserNotFoundException;
import org.example.feign.ProductServiceClient;
import org.example.feign.UserServiceClient;
import org.example.data.mapper.OrderMapper;
import org.example.repository.OrderRepository;
import org.example.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация интерфейса сервиса заказов.
 * Предоставляет методы для работы с заказами, корзинами и отправки событий.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final OrderMapper orderMapper;

    /**
     * {@inheritDoc}
     *
     * @throws UserNotFoundException если пользователь не найден
     * @throws InvalidOrderOperationException если выбранный адрес не принадлежит пользователю
     */
    @Override
    public OrderResponse addToCart(UUID userId, AddToCartRequest request) {
        log.debug("Добавление товаров в корзину пользователя с id: {}", userId);

        validateUserAddress(userId, request.addressId());

        OrderEntity order = orderRepository.findByUserIdAndStatus(userId, OrderStatus.CART)
                .orElseGet(() -> createNewCart(userId, request.addressId()));

        List<OrderItemEntity> newItems = orderMapper.toOrderItemEntityList(request.items());
        mergeItems(order.getItems(), newItems);

        order.setTotal(calculateTotal(order.getItems()));
        order.setUpdatedAt(LocalDateTime.now());

        order = orderRepository.save(order);
        log.debug("Товары добавлены в корзину с id: {}", order.getId());

        return orderMapper.toOrderResponse(order);
    }

    /**
     * {@inheritDoc}
     *
     * @throws OrderNotFoundException если заказ не найден
     * @throws InvalidOrderOperationException если заказ не в статусе корзины или товар не найден
     */
    @Override
    public OrderResponse removeFromCart(UUID orderId, UUID productId) {
        log.debug("Удаление товара с id: {} из заказа с id: {}", productId, orderId);

        OrderEntity order = getOrderOrThrow(orderId);
        validateCartOperation(order);

        boolean removed = order.getItems().removeIf(item -> item.getProductId().equals(productId));
        if (!removed) {
            log.error("Товар с id: {} не найден в заказе с id: {}", productId, orderId);
            throw new InvalidOrderOperationException(
                    String.format("Товар с id: %s не найден в заказе", productId));
        }

        order.setTotal(calculateTotal(order.getItems()));
        order.setUpdatedAt(LocalDateTime.now());

        order = orderRepository.save(order);
        log.debug("Товар удален из корзины с id: {}", orderId);

        return orderMapper.toOrderResponse(order);
    }

    /**
     * {@inheritDoc}
     *
     * @throws OrderNotFoundException если заказ не найден
     * @throws InvalidOrderOperationException если заказ не в статусе корзины или товар не найден
     */
    @Override
    public OrderResponse updateItemQuantity(UUID orderId, UUID productId, Integer quantity) {
        log.debug("Обновление количества товара с id: {} в заказе с id: {}", productId, orderId);

        OrderEntity order = getOrderOrThrow(orderId);
        validateCartOperation(order);

        OrderItemEntity itemToUpdate = order.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Товар с id: {} не найден в заказе с id: {}", productId, orderId);
                    return new InvalidOrderOperationException(
                            String.format("Товар с id: %s не найден в заказе", productId));
                });

        itemToUpdate.setQuantity(quantity);

        order.setTotal(calculateTotal(order.getItems()));
        order.setUpdatedAt(LocalDateTime.now());

        order = orderRepository.save(order);
        log.debug("Обновлено количество товара в корзине с id: {}", orderId);

        return orderMapper.toOrderResponse(order);
    }

    /**
     * {@inheritDoc}
     *
     * @throws OrderNotFoundException если заказ не найден
     * @throws InvalidOrderOperationException если заказ не в статусе корзины
     */
    @Override
    public OrderResponse clearCart(UUID orderId) {
        log.debug("Очистка заказа с id: {}", orderId);

        OrderEntity order = getOrderOrThrow(orderId);
        validateCartOperation(order);

        order.getItems().clear();
        order.setTotal(0.0);
        order.setUpdatedAt(LocalDateTime.now());

        order = orderRepository.save(order);
        log.debug("Заказ с id: {} очищен", orderId);

        return orderMapper.toOrderResponse(order);
    }

    /**
     * {@inheritDoc}
     *
     * @throws OrderNotFoundException если заказ не найден
     * @throws InvalidOrderOperationException если заказ не в статусе корзины или корзина пуста
     * @throws DataGettingException если произошла ошибка при получении данных от других сервисов
     */
    @Override
    public OrderResponse confirmOrder(UUID orderId) {
        log.debug("Подтверждение заказа с id: {}", orderId);

        OrderEntity order = getOrderOrThrow(orderId);
        validateCartOperation(order);

        if (order.getItems().isEmpty()) {
            log.error("Попытка подтвердить пустой заказ с id: {}", orderId);
            throw new InvalidOrderOperationException("Нельзя подтвердить пустой заказ");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        order.setUpdatedAt(LocalDateTime.now());

        order = orderRepository.save(order);
        OrderResponse orderResponse = orderMapper.toOrderResponse(order);

        sendOrderConfirmEvent(orderResponse);

        log.debug("Заказ с id: {} подтвержден", orderId);
        return orderResponse;
    }

    /**
     * {@inheritDoc}
     *
     * @throws OrderNotFoundException если заказ не найден
     * @throws InvalidOrderOperationException если заказ уже отменен или находится в статусе корзины
     * @throws DataGettingException если произошла ошибка при получении данных от других сервисов
     */
    @Override
    public OrderResponse cancelOrder(UUID orderId) {
        log.debug("Отмена заказа с id: {}", orderId);

        OrderEntity order = getOrderOrThrow(orderId);

        if (order.getStatus().equals(OrderStatus.CART)) {
            log.error("Попытка отменить корзину с id: {}", orderId);
            throw new InvalidOrderOperationException("Корзина не может быть отменена, используйте очистку");
        }
        if (order.getStatus().equals(OrderStatus.CANCELLED)) {
            log.error("Попытка отменить уже отмененный заказ с id: {}", orderId);
            throw new InvalidOrderOperationException("Заказ уже отменён");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        order = orderRepository.save(order);
        OrderResponse orderResponse = orderMapper.toOrderResponse(order);

        sendOrderCancelEvent(orderResponse);

        log.debug("Заказ с id: {} отменен", orderId);
        return orderResponse;
    }

    /**
     * {@inheritDoc}
     *
     * @throws OrderNotFoundException если заказ не найден
     */
    @Override
    public OrderResponse getOrderById(UUID orderId) {
        log.debug("Получение заказа с id: {}", orderId);

        OrderEntity order = getOrderOrThrow(orderId);
        log.debug("Получен заказ с id: {}", orderId);

        return orderMapper.toOrderResponse(order);
    }

    /**
     * {@inheritDoc}
     *
     * @throws OrderNotFoundException если корзина не найдена
     */
    @Override
    public OrderResponse getCartByUserId(UUID userId) {
        log.debug("Получение корзины пользователя с id: {}", userId);

        OrderEntity order = orderRepository.findByUserIdAndStatus(userId, OrderStatus.CART)
                .orElseThrow(() -> {
                    log.error("Корзина для пользователя с id: {} не найдена", userId);
                    return new OrderNotFoundException("Корзина для пользователя не найдена");
                });

        log.debug("Получена корзина пользователя с id: {}", userId);
        return orderMapper.toOrderResponse(order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<OrderSummaryResponse> getOrdersByUserId(UUID userId) {
        log.debug("Получение заказов пользователя с id: {}", userId);

        List<OrderEntity> orders = orderRepository.findAllByUserId(userId);
        log.debug("Получено {} заказов пользователя с id: {}", orders.size(), userId);

        return orderMapper.toOrderSummaryResponseList(orders);
    }

    /**
     * {@inheritDoc}
     *
     * @throws OrderNotFoundException если заказ не найден
     * @throws InvalidOrderOperationException если выбранный адрес не принадлежит пользователю
     */
    @Override
    public OrderResponse updateAddress(UUID orderId, UUID addressId) {
        log.debug("Обновление адреса доставки для заказа с id: {}", orderId);

        OrderEntity order = getOrderOrThrow(orderId);
        UUID userId = order.getUserId();

        validateUserAddress(userId, addressId);

        order.setAddressId(addressId);
        order.setUpdatedAt(LocalDateTime.now());

        order = orderRepository.save(order);
        log.debug("Обновлен адрес доставки для заказа с id: {}", orderId);

        return orderMapper.toOrderResponse(order);
    }

    /**
     * Создание новой корзины для пользователя.
     *
     * @param userId Уникальный идентификатор пользователя
     * @param addressId Уникальный идентификатор адреса доставки
     * @return Новая сущность заказа со статусом CART
     */
    private OrderEntity createNewCart(UUID userId, UUID addressId) {
        OrderEntity order = new OrderEntity();
        order.setId(UUID.randomUUID());
        order.setUserId(userId);
        order.setAddressId(addressId);
        order.setStatus(OrderStatus.CART);
        order.setItems(new ArrayList<>());
        order.setTotal(0.0);

        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        return order;
    }

    /**
     * Проверка принадлежности адреса пользователю.
     *
     * @param userId Уникальный идентификатор пользователя
     * @param addressId Уникальный идентификатор адреса
     * @throws UserNotFoundException если пользователь не найден
     * @throws InvalidOrderOperationException если адрес не принадлежит пользователю
     */
    private void validateUserAddress(UUID userId, UUID addressId) {
        try {
            ResponseEntity<Set<AddressResponse>> response = userServiceClient.getUserAddresses(userId);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new UserNotFoundException(String.format("Пользователь с id: %s не найден", userId));
            }

            Set<AddressResponse> addresses = response.getBody();
            boolean addressBelongsToUser = addresses.stream()
                    .anyMatch(address -> address.id().equals(addressId));

            if (!addressBelongsToUser) {
                throw new InvalidOrderOperationException("Выбранный адрес не принадлежит пользователю");
            }
        } catch (FeignException e) {
            log.error("Ошибка при вызове USER-SERVICE: status: {}, message: {}", e.status(), e.getMessage());
            throw new RuntimeException("Ошибка при обращении к сервису пользователей");
        }
    }

    /**
     * Получение заказа или генерация исключения.
     *
     * @param orderId Уникальный идентификатор заказа
     * @return Сущность заказа
     * @throws OrderNotFoundException если заказ не найден
     */
    private OrderEntity getOrderOrThrow(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Заказ с id: {} не найден", orderId);
                    return new OrderNotFoundException(String.format("Заказ с id: %s не найден", orderId));
                });
    }

    /**
     * Проверка возможности операции с корзиной.
     *
     * @param order Сущность заказа
     * @throws InvalidOrderOperationException если заказ не в статусе корзины
     */
    private void validateCartOperation(OrderEntity order) {
        if (!order.getStatus().equals(OrderStatus.CART)) {
            log.error("Попытка изменить заказ с id: {} в статусе: {}", order.getId(), order.getStatus());
            throw new InvalidOrderOperationException("Операция доступна только для корзины");
        }
    }

    /**
     * Объединение списков товаров с суммированием количества.
     *
     * @param existingItems Существующий список товаров
     * @param newItems Новый список товаров для добавления
     */
    private void mergeItems(List<OrderItemEntity> existingItems, List<OrderItemEntity> newItems) {
        for (OrderItemEntity newItem : newItems) {
            existingItems.stream()
                    .filter(item -> item.getProductId().equals(newItem.getProductId()))
                    .findFirst()
                    .ifPresentOrElse(
                            item -> item.setQuantity(item.getQuantity() + newItem.getQuantity()),
                            () -> existingItems.add(newItem)
                    );
        }
    }

    /**
     * Расчет общей суммы заказа.
     *
     * @param items Список товаров в заказе
     * @return Общая сумма заказа
     */
    private Double calculateTotal(List<OrderItemEntity> items) {
        return items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    /**
     * Отправка события подтверждения заказа в Kafka.
     *
     * @param orderResponse DTO заказа для формирования события
     * @throws DataGettingException если произошла ошибка при получении данных от других сервисов
     */
    private void sendOrderConfirmEvent(OrderResponse orderResponse) {
        try {
            OrderConfirmEvent event = createOrderConfirmEvent(orderResponse);
            kafkaTemplate.send("order-events-topic", event);
            log.debug("Отправлено событие подтверждения заказа с id: {}", orderResponse.id());
        } catch (Exception e) {
            log.error("Ошибка при отправке события подтверждения заказа: {}", e.getMessage(), e);
        }
    }

    /**
     * Отправка события отмены заказа в Kafka.
     *
     * @param orderResponse DTO заказа для формирования события
     * @throws DataGettingException если произошла ошибка при получении данных от других сервисов
     */
    private void sendOrderCancelEvent(OrderResponse orderResponse) {
        try {
            OrderCancelEvent event = createOrderCancelEvent(orderResponse);
            kafkaTemplate.send("order-events-topic", event);
            log.debug("Отправлено событие отмены заказа с id: {}", orderResponse.id());
        } catch (Exception e) {
            log.error("Ошибка при отправке события отмены заказа: {}", e.getMessage(), e);
        }
    }

    /**
     * Создание события подтверждения заказа.
     *
     * @param orderResponse DTO заказа для формирования события
     * @return Событие подтверждения заказа
     * @throws DataGettingException если произошла ошибка при получении данных от других сервисов
     */
    private OrderConfirmEvent createOrderConfirmEvent(OrderResponse orderResponse) {
        OrderConfirmEvent event = new OrderConfirmEvent();
        event.setUserId(orderResponse.userId());
        event.setOrderId(orderResponse.id());
        event.setTotalPrice(orderResponse.total());

        try {
            ResponseEntity<UserDetailResponse> userResponse = userServiceClient.getUserDetailsById(orderResponse.userId());
            if (userResponse.getStatusCode().is2xxSuccessful() && userResponse.getBody() != null) {
                UserDetailResponse user = userResponse.getBody();
                AddressResponse address = user.addresses().stream()
                        .filter(a -> a.id().equals(orderResponse.addressId()))
                        .findFirst()
                        .orElseThrow(() -> new AddressNotFoundException("Не удалось получить адрес пользователя"));

                event.setEmail(user.email());
                event.setUsername(user.firstName());
                event.setFullAddress(String.format("%s, %s, %s", address.street(), address.city(), address.country()));
            }
        } catch (FeignException e) {
            log.error("Ошибка при получении данных пользователя: {}", e.getMessage());
            throw new DataGettingException("Ошибка при получении данных пользователя");
        }

        List<UUID> productIds = orderResponse.items().stream()
                .map(OrderItemResponse::productId)
                .collect(Collectors.toList());

        Map<UUID, ProductDto> productsById = new HashMap<>();

        if (!productIds.isEmpty()) {
            try {
                ResponseEntity<List<ProductDto>> productsResponse = productServiceClient.getProductsByIds(productIds);
                if (productsResponse.getStatusCode().is2xxSuccessful() && productsResponse.getBody() != null) {
                    productsResponse.getBody().forEach(product -> productsById.put(product.id(), product));
                }
            } catch (FeignException e) {
                log.error("Ошибка при получении данных товаров: {}", e.getMessage());
                throw new DataGettingException("Ошибка при получении данных товаров");
            }
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemResponse item : orderResponse.items()) {
            OrderItem orderItem = new OrderItem();

            ProductDto product = productsById.get(item.productId());
            if (product != null) {
                orderItem.setName(product.name());
            } else {
                orderItem.setName("Неизвестный товар");
            }

            orderItem.setQuantity(item.quantity());
            orderItem.setPrice(item.price());
            orderItems.add(orderItem);
        }

        event.setItems(orderItems);
        return event;
    }

    /**
     * Создание события отмены заказа.
     *
     * @param orderResponse DTO заказа для формирования события
     * @return Событие отмены заказа
     * @throws DataGettingException если произошла ошибка при получении данных от других сервисов
     */
    private OrderCancelEvent createOrderCancelEvent(OrderResponse orderResponse) {
        OrderCancelEvent event = new OrderCancelEvent();
        event.setUserId(orderResponse.userId());
        event.setOrderId(orderResponse.id());
        event.setTotalPrice(orderResponse.total());

        try {
            ResponseEntity<UserDetailResponse> userResponse = userServiceClient.getUserDetailsById(orderResponse.userId());
            if (userResponse.getStatusCode().is2xxSuccessful() && userResponse.getBody() != null) {
                UserDetailResponse user = userResponse.getBody();
                event.setEmail(user.email());
                event.setUsername(user.firstName());
            }
        } catch (FeignException e) {
            log.error("Ошибка при получении данных пользователя: {}", e.getMessage());
            throw new DataGettingException("Ошибка при получении данных товаров");
        }

        return event;
    }
}
