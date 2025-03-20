package org.example.service.impl;

import org.example.data.dto.feign.product.ProductDto;
import org.example.data.dto.feign.user.AddressResponse;
import org.example.data.dto.feign.user.UserDetailResponse;
import org.example.data.dto.order.*;
import org.example.data.entity.OrderEntity;
import org.example.data.entity.OrderItemEntity;
import org.example.data.event.OrderEvent;
import org.example.data.event.type.OrderCancelEvent;
import org.example.data.event.type.OrderConfirmEvent;
import org.example.data.mapper.OrderMapper;
import org.example.exception.order.InvalidOrderOperationException;
import org.example.exception.order.OrderNotFoundException;
import org.example.feign.ProductServiceClient;
import org.example.feign.UserServiceClient;
import org.example.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UUID orderId;
    private UUID userId;
    private UUID productId;
    private UUID addressId;
    private OrderEntity orderEntity;
    private OrderItemEntity orderItemEntity;
    private OrderResponse orderResponse;
    private OrderSummaryResponse orderSummaryResponse;
    private OrderItemResponse orderItemResponse;
    private AddToCartRequest addToCartRequest;
    private AddressResponse addressResponse;
    private UserDetailResponse userDetailResponse;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        addressId = UUID.randomUUID();

        orderItemEntity = new OrderItemEntity();
        orderItemEntity.setProductId(productId);
        orderItemEntity.setQuantity(2);
        orderItemEntity.setPrice(100.0);

        orderEntity = new OrderEntity();
        orderEntity.setId(orderId);
        orderEntity.setUserId(userId);
        orderEntity.setItems(new ArrayList<>(Collections.singletonList(orderItemEntity)));
        orderEntity.setTotal(200.0);
        orderEntity.setStatus(OrderStatus.CART);
        orderEntity.setAddressId(addressId);
        orderEntity.setCreatedAt(LocalDateTime.now());
        orderEntity.setUpdatedAt(LocalDateTime.now());

        orderItemResponse = new OrderItemResponse(
                productId,
                2,
                100.0,
                200.0
        );

        orderResponse = new OrderResponse(
                orderId,
                userId,
                List.of(orderItemResponse),
                200.0,
                OrderStatus.CART,
                addressId,
                orderEntity.getCreatedAt(),
                orderEntity.getUpdatedAt()
        );

        orderSummaryResponse = new OrderSummaryResponse(
                orderId,
                userId,
                1,
                200.0,
                OrderStatus.CART,
                orderEntity.getCreatedAt()
        );

        OrderItemRequest orderItemRequest = new OrderItemRequest(
                productId,
                2,
                100.0
        );

        addToCartRequest = new AddToCartRequest(
                List.of(orderItemRequest),
                addressId
        );

        addressResponse = new AddressResponse(
                addressId,
                "Street 123",
                "Apt 456",
                "City",
                "12345",
                true
        );

        userDetailResponse = new UserDetailResponse(
                userId,
                "John",
                "Doe",
                "john.doe@example.com",
                "+1234567890",
                Set.of(addressResponse)
        );
    }

    @Test
    void addToCart_WhenCartDoesNotExist_ShouldCreateNewCart() {
        ResponseEntity<Set<AddressResponse>> addressesResponse = ResponseEntity.ok(Set.of(addressResponse));
        when(userServiceClient.getUserAddresses(userId)).thenReturn(addressesResponse);
        when(orderRepository.findByUserIdAndStatus(userId, OrderStatus.CART)).thenReturn(Optional.empty());
        when(orderMapper.toOrderItemEntityList(addToCartRequest.items())).thenReturn(List.of(orderItemEntity));
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderEntity);
        when(orderMapper.toOrderResponse(any(OrderEntity.class))).thenReturn(orderResponse);

        OrderResponse result = orderService.addToCart(userId, addToCartRequest);

        assertThat(result).isNotNull();
        assertEquals(orderResponse, result);

        verify(userServiceClient).getUserAddresses(userId);
        verify(orderRepository).findByUserIdAndStatus(userId, OrderStatus.CART);
        verify(orderMapper).toOrderItemEntityList(addToCartRequest.items());
        verify(orderRepository).save(any(OrderEntity.class));
        verify(orderMapper).toOrderResponse(any(OrderEntity.class));
    }

    @Test
    void addToCart_WhenCartExists_ShouldAddItemsToExistingCart() {
        ResponseEntity<Set<AddressResponse>> addressesResponse = ResponseEntity.ok(Set.of(addressResponse));
        when(userServiceClient.getUserAddresses(userId)).thenReturn(addressesResponse);
        when(orderRepository.findByUserIdAndStatus(userId, OrderStatus.CART)).thenReturn(Optional.of(orderEntity));
        when(orderMapper.toOrderItemEntityList(addToCartRequest.items())).thenReturn(List.of(orderItemEntity));
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderEntity);
        when(orderMapper.toOrderResponse(orderEntity)).thenReturn(orderResponse);

        OrderResponse result = orderService.addToCart(userId, addToCartRequest);

        assertThat(result).isNotNull();
        assertEquals(orderResponse, result);

        verify(userServiceClient).getUserAddresses(userId);
        verify(orderRepository).findByUserIdAndStatus(userId, OrderStatus.CART);
        verify(orderMapper).toOrderItemEntityList(addToCartRequest.items());
        verify(orderRepository).save(any(OrderEntity.class));
        verify(orderMapper).toOrderResponse(any(OrderEntity.class));
    }

    @Test
    void removeFromCart_ShouldBeSuccess() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(orderRepository.save(orderEntity)).thenReturn(orderEntity);
        when(orderMapper.toOrderResponse(orderEntity)).thenReturn(orderResponse);

        OrderResponse result = orderService.removeFromCart(orderId, productId);

        assertThat(result).isNotNull();
        assertEquals(orderResponse, result);

        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(any(OrderEntity.class));
        verify(orderMapper).toOrderResponse(any(OrderEntity.class));
    }

    @Test
    void removeFromCart_WhenProductNotFoundInCart_ShouldThrowException() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));

        UUID differentProductId = UUID.randomUUID();

        assertThrows(InvalidOrderOperationException.class, () -> orderService.removeFromCart(orderId, differentProductId));

        verify(orderRepository).findById(orderId);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(orderMapper);
    }

    @Test
    void updateItemQuantity_ShouldBeSuccess() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(orderRepository.save(orderEntity)).thenReturn(orderEntity);
        when(orderMapper.toOrderResponse(orderEntity)).thenReturn(orderResponse);

        OrderResponse result = orderService.updateItemQuantity(orderId, productId, 3);

        assertThat(result).isNotNull();
        assertEquals(orderResponse, result);
        assertEquals(3, orderEntity.getItems().get(0).getQuantity());

        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(any(OrderEntity.class));
        verify(orderMapper).toOrderResponse(any(OrderEntity.class));
    }

    @Test
    void updateItemQuantity_WhenProductNotFoundInCart_ShouldThrowException() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));

        UUID differentProductId = UUID.randomUUID();

        assertThrows(InvalidOrderOperationException.class,
                () -> orderService.updateItemQuantity(orderId, differentProductId, 3));

        verify(orderRepository).findById(orderId);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(orderMapper);
    }

    @Test
    void clearCart_ShouldBeSuccess() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(orderRepository.save(orderEntity)).thenReturn(orderEntity);
        when(orderMapper.toOrderResponse(orderEntity)).thenReturn(orderResponse);

        OrderResponse result = orderService.clearCart(orderId);

        assertThat(result).isNotNull();
        assertEquals(orderResponse, result);
        assertTrue(orderEntity.getItems().isEmpty());
        assertEquals(0.0, orderEntity.getTotal());

        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(any(OrderEntity.class));
        verify(orderMapper).toOrderResponse(any(OrderEntity.class));
    }

    @Test
    void confirmOrder_ShouldBeSuccess() {
        OrderResponse confirmedOrderResponse = new OrderResponse(
                orderId,
                userId,
                List.of(orderItemResponse),
                200.0,
                OrderStatus.CONFIRMED,
                addressId,
                orderEntity.getCreatedAt(),
                orderEntity.getUpdatedAt()
        );
        ResponseEntity<UserDetailResponse> userResponse = ResponseEntity.ok(userDetailResponse);

        ResponseEntity<List<ProductDto>> productResponse = ResponseEntity.ok(List.of(
                new ProductDto(productId, "Test Product")
        ));

        orderEntity.setItems(List.of(orderItemEntity));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(orderRepository.save(orderEntity)).thenReturn(orderEntity);
        when(orderMapper.toOrderResponse(orderEntity)).thenReturn(confirmedOrderResponse);
        when(userServiceClient.getUserDetailsById(userId)).thenReturn(userResponse);
        when(productServiceClient.getProductsByIds(List.of(productId))).thenReturn(productResponse);
        when(kafkaTemplate.send(eq("order-events-topic"), any(OrderConfirmEvent.class))).thenReturn(null);

        OrderResponse result = orderService.confirmOrder(orderId);

        assertThat(result).isNotNull();
        assertEquals(confirmedOrderResponse, result);
        assertEquals(OrderStatus.CONFIRMED, orderEntity.getStatus());

        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(any(OrderEntity.class));
        verify(orderMapper).toOrderResponse(any(OrderEntity.class));
        verify(userServiceClient).getUserDetailsById(userId);
        verify(productServiceClient).getProductsByIds(List.of(productId));
        verify(kafkaTemplate).send(eq("order-events-topic"), any(OrderConfirmEvent.class));
    }

    @Test
    void confirmOrder_WhenCartIsEmpty_ShouldThrowException() {
        orderEntity.setItems(new ArrayList<>());
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));

        assertThrows(InvalidOrderOperationException.class, () -> orderService.confirmOrder(orderId));

        verify(orderRepository).findById(orderId);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(orderMapper);
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void cancelOrder_ShouldBeSuccess() {
        OrderResponse cancelledOrderResponse = new OrderResponse(
                orderId,
                userId,
                List.of(orderItemResponse),
                200.0,
                OrderStatus.CANCELLED,
                addressId,
                orderEntity.getCreatedAt(),
                orderEntity.getUpdatedAt()
        );

        ResponseEntity<UserDetailResponse> userResponse = ResponseEntity.ok(userDetailResponse);
        orderEntity.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(orderRepository.save(orderEntity)).thenReturn(orderEntity);
        when(orderMapper.toOrderResponse(orderEntity)).thenReturn(cancelledOrderResponse);
        when(userServiceClient.getUserDetailsById(userId)).thenReturn(userResponse);
        when(kafkaTemplate.send(eq("order-events-topic"), any(OrderCancelEvent.class))).thenReturn(null);

        OrderResponse result = orderService.cancelOrder(orderId);

        assertThat(result).isNotNull();
        assertEquals(cancelledOrderResponse, result);
        assertEquals(OrderStatus.CANCELLED, orderEntity.getStatus());

        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(any(OrderEntity.class));
        verify(orderMapper).toOrderResponse(any(OrderEntity.class));
        verify(userServiceClient).getUserDetailsById(userId);
        verify(kafkaTemplate).send(eq("order-events-topic"), any(OrderCancelEvent.class));
    }

    @Test
    void cancelOrder_WhenCartStatus_ShouldThrowException() {
        orderEntity.setStatus(OrderStatus.CART);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));

        assertThrows(InvalidOrderOperationException.class, () -> orderService.cancelOrder(orderId));

        verify(orderRepository).findById(orderId);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(orderMapper);
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void getOrderById_ShouldBeSuccess() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(orderMapper.toOrderResponse(orderEntity)).thenReturn(orderResponse);

        OrderResponse result = orderService.getOrderById(orderId);

        assertThat(result).isNotNull();
        assertEquals(orderResponse, result);

        verify(orderRepository).findById(orderId);
        verify(orderMapper).toOrderResponse(any(OrderEntity.class));
    }

    @Test
    void getOrderById_WhenOrderNotFound_ShouldThrowException() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(orderId));

        verify(orderRepository).findById(orderId);
        verifyNoInteractions(orderMapper);
    }

    @Test
    void getCartByUserId_ShouldBeSuccess() {
        when(orderRepository.findByUserIdAndStatus(userId, OrderStatus.CART)).thenReturn(Optional.of(orderEntity));
        when(orderMapper.toOrderResponse(orderEntity)).thenReturn(orderResponse);

        OrderResponse result = orderService.getCartByUserId(userId);

        assertThat(result).isNotNull();
        assertEquals(orderResponse, result);

        verify(orderRepository).findByUserIdAndStatus(userId, OrderStatus.CART);
        verify(orderMapper).toOrderResponse(any(OrderEntity.class));
    }

    @Test
    void getCartByUserId_WhenCartNotFound_ShouldThrowException() {
        when(orderRepository.findByUserIdAndStatus(userId, OrderStatus.CART)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getCartByUserId(userId));

        verify(orderRepository).findByUserIdAndStatus(userId, OrderStatus.CART);
        verifyNoInteractions(orderMapper);
    }

    @Test
    void getOrdersByUserId_ShouldBeSuccess() {
        List<OrderEntity> orderEntityList = List.of(orderEntity);
        List<OrderSummaryResponse> orderSummaryResponses = List.of(orderSummaryResponse);

        when(orderRepository.findAllByUserId(userId)).thenReturn(orderEntityList);
        when(orderMapper.toOrderSummaryResponseList(orderEntityList)).thenReturn(orderSummaryResponses);

        List<OrderSummaryResponse> results = orderService.getOrdersByUserId(userId);

        assertThat(results).isNotNull().hasSize(1);
        assertEquals(orderSummaryResponse, results.get(0));

        verify(orderRepository).findAllByUserId(userId);
        verify(orderMapper).toOrderSummaryResponseList(orderEntityList);
    }

    @Test
    void updateAddress_ShouldBeSuccess() {
        UUID newAddressId = UUID.randomUUID();
        AddressResponse newAddressResponse = new AddressResponse(
                newAddressId,
                "ул.Чехова д.5",
                "Москва",
                "Россия",
                "10001",
                true
        );

        OrderResponse updatedAddressResponse = new OrderResponse(
                orderId,
                userId,
                List.of(orderItemResponse),
                200.0,
                OrderStatus.CART,
                newAddressId,
                orderEntity.getCreatedAt(),
                orderEntity.getUpdatedAt()
        );

        ResponseEntity<Set<AddressResponse>> addressesResponse = ResponseEntity.ok(Set.of(newAddressResponse));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(userServiceClient.getUserAddresses(userId)).thenReturn(addressesResponse);
        when(orderRepository.save(orderEntity)).thenReturn(orderEntity);
        when(orderMapper.toOrderResponse(orderEntity)).thenReturn(updatedAddressResponse);

        OrderResponse result = orderService.updateAddress(orderId, newAddressId);

        assertThat(result).isNotNull();
        assertEquals(updatedAddressResponse, result);
        assertEquals(newAddressId, orderEntity.getAddressId());

        verify(orderRepository).findById(orderId);
        verify(userServiceClient).getUserAddresses(userId);
        verify(orderRepository).save(any(OrderEntity.class));
        verify(orderMapper).toOrderResponse(any(OrderEntity.class));
    }

    @Test
    void updateAddress_WhenAddressDoesNotBelongToUser_ShouldThrowException() {
        UUID newAddressId = UUID.randomUUID();
        ResponseEntity<Set<AddressResponse>> addressesResponse = ResponseEntity.ok(Set.of(addressResponse));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(userServiceClient.getUserAddresses(userId)).thenReturn(addressesResponse);

        assertThrows(InvalidOrderOperationException.class, () -> orderService.updateAddress(orderId, newAddressId));

        verify(orderRepository).findById(orderId);
        verify(userServiceClient).getUserAddresses(userId);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(orderMapper);
    }
}