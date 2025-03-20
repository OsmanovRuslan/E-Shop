package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.data.dto.order.*;
import org.example.exception.order.InvalidOrderOperationException;
import org.example.exception.order.OrderNotFoundException;
import org.example.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID orderId;
    private UUID userId;
    private UUID productId;
    private UUID addressId;
    private OrderResponse orderResponse;
    private OrderSummaryResponse orderSummaryResponse;
    private AddToCartRequest addToCartRequest;
    private UpdateQuantityRequest updateQuantityRequest;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        addressId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        OrderItemResponse orderItemResponse = new OrderItemResponse(
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
                now,
                now
        );

        orderSummaryResponse = new OrderSummaryResponse(
                orderId,
                userId,
                1,
                200.0,
                OrderStatus.CART,
                now
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

        updateQuantityRequest = new UpdateQuantityRequest(3);
    }

    @Test
    void getOrderById_ShouldReturnOk() throws Exception {
        when(orderService.getOrderById(orderId)).thenReturn(orderResponse);

        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.total").value(200.0))
                .andExpect(jsonPath("$.status").value("CART"))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productId").value(productId.toString()));

        verify(orderService).getOrderById(orderId);
    }

    @Test
    void getOrderById_WhenOrderNotFound_ShouldReturnNotFound() throws Exception {
        when(orderService.getOrderById(orderId)).thenThrow(
                new OrderNotFoundException(String.format("Заказ с id: %s не найден", orderId)));

        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isNotFound());

        verify(orderService).getOrderById(orderId);
    }

    @Test
    void getOrdersByUserId_ShouldReturnOk() throws Exception {
        when(orderService.getOrdersByUserId(userId)).thenReturn(List.of(orderSummaryResponse));

        mockMvc.perform(get("/api/v1/orders/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(orderId.toString()))
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$[0].total").value(200.0))
                .andExpect(jsonPath("$[0].status").value("CART"));

        verify(orderService).getOrdersByUserId(userId);
    }

    @Test
    void getCartByUserId_ShouldReturnOk() throws Exception {
        when(orderService.getCartByUserId(userId)).thenReturn(orderResponse);

        mockMvc.perform(get("/api/v1/orders/user/{userId}/cart", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.total").value(200.0))
                .andExpect(jsonPath("$.status").value("CART"));

        verify(orderService).getCartByUserId(userId);
    }

    @Test
    void getCartByUserId_WhenCartNotFound_ShouldReturnNotFound() throws Exception {
        when(orderService.getCartByUserId(userId)).thenThrow(
                new OrderNotFoundException("Корзина для пользователя не найдена"));

        mockMvc.perform(get("/api/v1/orders/user/{userId}/cart", userId))
                .andExpect(status().isNotFound());

        verify(orderService).getCartByUserId(userId);
    }

    @Test
    void addToCart_ShouldReturnOk() throws Exception {
        when(orderService.addToCart(eq(userId), any(AddToCartRequest.class))).thenReturn(orderResponse);

        mockMvc.perform(post("/api/v1/orders/user/{userId}/cart", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.total").value(200.0))
                .andExpect(jsonPath("$.status").value("CART"));

        verify(orderService).addToCart(eq(userId), any(AddToCartRequest.class));
    }

    @Test
    void removeFromCart_ShouldReturnOk() throws Exception {
        when(orderService.removeFromCart(orderId, productId)).thenReturn(orderResponse);

        mockMvc.perform(delete("/api/v1/orders/{orderId}/items/{productId}", orderId, productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.total").value(200.0))
                .andExpect(jsonPath("$.status").value("CART"));

        verify(orderService).removeFromCart(orderId, productId);
    }

    @Test
    void removeFromCart_WhenProductNotInCart_ShouldReturnBadRequest() throws Exception {
        when(orderService.removeFromCart(orderId, productId)).thenThrow(
                new InvalidOrderOperationException(String.format("Товар с id: %s не найден в заказе", productId)));

        mockMvc.perform(delete("/api/v1/orders/{orderId}/items/{productId}", orderId, productId))
                .andExpect(status().isBadRequest());

        verify(orderService).removeFromCart(orderId, productId);
    }

    @Test
    void updateItemQuantity_ShouldReturnOk() throws Exception {
        when(orderService.updateItemQuantity(orderId, productId, updateQuantityRequest.quantity())).thenReturn(orderResponse);

        mockMvc.perform(put("/api/v1/orders/{orderId}/items/{productId}", orderId, productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateQuantityRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.total").value(200.0))
                .andExpect(jsonPath("$.status").value("CART"));

        verify(orderService).updateItemQuantity(orderId, productId, updateQuantityRequest.quantity());
    }

    @Test
    void updateItemQuantity_WhenProductNotInCart_ShouldReturnBadRequest() throws Exception {
        when(orderService.updateItemQuantity(orderId, productId, updateQuantityRequest.quantity())).thenThrow(
                new InvalidOrderOperationException(String.format("Товар с id: %s не найден в заказе", productId)));

        mockMvc.perform(put("/api/v1/orders/{orderId}/items/{productId}", orderId, productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateQuantityRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService).updateItemQuantity(orderId, productId, updateQuantityRequest.quantity());
    }

    @Test
    void clearCart_ShouldReturnOk() throws Exception {
        when(orderService.clearCart(orderId)).thenReturn(orderResponse);

        mockMvc.perform(delete("/api/v1/orders/{orderId}/items", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.total").value(200.0))
                .andExpect(jsonPath("$.status").value("CART"));

        verify(orderService).clearCart(orderId);
    }

    @Test
    void confirmOrder_ShouldReturnOk() throws Exception {
        OrderResponse confirmedOrderResponse = new OrderResponse(
                orderId,
                userId,
                orderResponse.items(),
                orderResponse.total(),
                OrderStatus.CONFIRMED,
                addressId,
                orderResponse.createdAt(),
                orderResponse.updatedAt()
        );

        when(orderService.confirmOrder(orderId)).thenReturn(confirmedOrderResponse);

        mockMvc.perform(post("/api/v1/orders/{orderId}/confirm", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.total").value(200.0))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(orderService).confirmOrder(orderId);
    }

    @Test
    void confirmOrder_WhenEmptyCart_ShouldReturnBadRequest() throws Exception {
        when(orderService.confirmOrder(orderId)).thenThrow(
                new InvalidOrderOperationException("Нельзя подтвердить пустой заказ"));

        mockMvc.perform(post("/api/v1/orders/{orderId}/confirm", orderId))
                .andExpect(status().isBadRequest());

        verify(orderService).confirmOrder(orderId);
    }

    @Test
    void cancelOrder_ShouldReturnOk() throws Exception {
        OrderResponse canceledOrderResponse = new OrderResponse(
                orderId,
                userId,
                orderResponse.items(),
                orderResponse.total(),
                OrderStatus.CANCELLED,
                addressId,
                orderResponse.createdAt(),
                orderResponse.updatedAt()
        );

        when(orderService.cancelOrder(orderId)).thenReturn(canceledOrderResponse);

        mockMvc.perform(post("/api/v1/orders/{orderId}/cancel", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.total").value(200.0))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(orderService).cancelOrder(orderId);
    }

    @Test
    void cancelOrder_WhenCartStatus_ShouldReturnBadRequest() throws Exception {
        when(orderService.cancelOrder(orderId)).thenThrow(
                new InvalidOrderOperationException("Корзина не может быть отменена, используйте очистку"));

        mockMvc.perform(post("/api/v1/orders/{orderId}/cancel", orderId))
                .andExpect(status().isBadRequest());

        verify(orderService).cancelOrder(orderId);
    }

    @Test
    void updateAddress_ShouldReturnOk() throws Exception {
        UUID newAddressId = UUID.randomUUID();
        when(orderService.updateAddress(orderId, newAddressId)).thenReturn(orderResponse);

        mockMvc.perform(put("/api/v1/orders/{orderId}/address/{addressId}", orderId, newAddressId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.total").value(200.0))
                .andExpect(jsonPath("$.status").value("CART"));

        verify(orderService).updateAddress(orderId, newAddressId);
    }

    @Test
    void updateAddress_WhenAddressDoesNotBelongToUser_ShouldReturnBadRequest() throws Exception {
        UUID newAddressId = UUID.randomUUID();
        when(orderService.updateAddress(orderId, newAddressId)).thenThrow(
                new InvalidOrderOperationException("Выбранный адрес не принадлежит пользователю"));

        mockMvc.perform(put("/api/v1/orders/{orderId}/address/{addressId}", orderId, newAddressId))
                .andExpect(status().isBadRequest());

        verify(orderService).updateAddress(orderId, newAddressId);
    }
}