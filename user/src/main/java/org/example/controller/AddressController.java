package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.data.dto.address.AddressCreateRequest;
import org.example.data.dto.address.AddressResponse;
import org.example.data.dto.address.AddressUpdateRequest;
import org.example.service.AddressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

/**
 * Контроллер для управления адресами пользователей.
 * Предоставляет API для создания, получения, обновления и удаления адресов пользователей.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/{userId}/addresses")
public class AddressController {

    private final AddressService addressService;

    /**
     * Создание нового адреса для пользователя.
     * Доступно только аутентифицированным пользователям.
     *
     * @param userId Уникальный идентификатор пользователя
     * @param request Данные для создания адреса
     * @return ResponseEntity с информацией о созданном адресе
     */
    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(@PathVariable UUID userId, @RequestBody AddressCreateRequest request) {
        log.info("Получен запрос на создание адреса для пользователя с id: {}", userId);
        AddressResponse response = addressService.addAddress(userId, request);
        log.info("Создан адрес с id: {} для пользователя с id: {}", response.id(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Получение адреса пользователя по ID.
     * Доступно только аутентифицированным пользователям.
     *
     * @param userId Уникальный идентификатор пользователя
     * @param addressId Уникальный идентификатор адреса
     * @return ResponseEntity с информацией об адресе
     */
    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponse> getAddress(@PathVariable UUID userId, @PathVariable UUID addressId) {
        log.info("Получен запрос на получение адреса с id: {} пользователя с id: {}", addressId, userId);
        AddressResponse response = addressService.getAddressById(userId, addressId);
        log.info("Получен адрес с id: {}", addressId);
        return ResponseEntity.ok(response);
    }

    /**
     * Получение всех адресов пользователя.
     * Доступно только аутентифицированным пользователям.
     *
     * @param userId Уникальный идентификатор пользователя
     * @return ResponseEntity с набором адресов пользователя
     */
    @GetMapping
    public ResponseEntity<Set<AddressResponse>> getUserAddresses(@PathVariable UUID userId) {
        log.info("Получен запрос на получение списка адресов пользователя с id: {}", userId);
        Set<AddressResponse> responses = addressService.getUserAddresses(userId);
        log.info("Получены все адреса для пользователя с id: {}", userId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Обновление адреса пользователя.
     * Доступно только аутентифицированным пользователям.
     *
     * @param userId Уникальный идентификатор пользователя
     * @param addressId Уникальный идентификатор адреса
     * @param request Данные для обновления адреса
     * @return ResponseEntity с обновленной информацией об адресе
     */
    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(@PathVariable UUID userId, @PathVariable UUID addressId, @RequestBody AddressUpdateRequest request) {
        log.info("Получен запрос на обновление адреса с id: {} пользователя с id: {}", addressId, userId);
        AddressResponse response = addressService.updateAddress(userId, addressId, request);
        log.info("Обновлен адрес с id: {}", addressId);
        return ResponseEntity.ok(response);
    }

    /**
     * Удаление адреса пользователя.
     * Доступно только аутентифицированным пользователям.
     *
     * @param userId Уникальный идентификатор пользователя
     * @param addressId Уникальный идентификатор адреса
     * @return ResponseEntity без тела ответа
     */
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable UUID userId, @PathVariable UUID addressId) {
        log.info("Получен запрос на удаление адреса с id: {} пользователя с id: {}", addressId, userId);
        addressService.deleteAddress(userId, addressId);
        log.info("Пользователь с id: {} удален", addressId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Установка адреса пользователя как адреса по умолчанию.
     * Доступно только аутентифицированным пользователям.
     *
     * @param userId Уникальный идентификатор пользователя
     * @param addressId Уникальный идентификатор адреса
     * @return ResponseEntity без тела ответа
     */
    @PutMapping("/{addressId}/default")
    public ResponseEntity<Void> setAsDefaultAddress(@PathVariable UUID userId, @PathVariable UUID addressId) {
        log.info("Получен запрос на установку адреса с id: {} дефолтным для пользователя с id: {}", addressId, userId);
        addressService.setAsDefaultAddress(userId, addressId);
        log.info("Адрес с id: {} установлен дефолтным", addressId);
        return ResponseEntity.ok().build();
    }
}
