package org.example.feign;

import feign.FeignException;
import org.example.data.dto.feign.user.AddressResponse;
import org.example.data.dto.feign.user.UserDetailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Set;
import java.util.UUID;

/**
 * Клиент для взаимодействия с User Service через Feign.
 * Позволяет получать информацию о пользователях и их адресах.
 */
@FeignClient(name = "user-service")
public interface UserServiceClient {

    /**
     * Получение всех адресов пользователя.
     *
     * @param userId Уникальный идентификатор пользователя
     * @return ResponseEntity с набором адресов пользователя
     * @throws FeignException если произошла ошибка при вызове User Service
     */
    @GetMapping("/api/v1/users/{userId}/addresses")
    ResponseEntity<Set<AddressResponse>> getUserAddresses(@PathVariable UUID userId);

    /**
     * Получение адреса по ID.
     *
     * @param addressId Уникальный идентификатор адреса
     * @return ResponseEntity с информацией об адресе
     * @throws FeignException если произошла ошибка при вызове User Service
     */
    @GetMapping("/api/v1/addresses/{addressId}")
    ResponseEntity<AddressResponse> getAddress(@PathVariable UUID addressId);

    /**
     * Получение пользователя по ID.
     *
     * @param userId Уникальный идентификатор пользователя
     * @return ResponseEntity с детальной информацией о пользователе
     * @throws FeignException если произошла ошибка при вызове User Service
     */
    @GetMapping("/api/v1/users/{userId}")
    ResponseEntity<UserDetailResponse> getUserDetailsById(@PathVariable UUID userId);
}