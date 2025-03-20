package org.example.service;

import org.example.data.dto.address.AddressCreateRequest;
import org.example.data.dto.address.AddressResponse;
import org.example.data.dto.address.AddressUpdateRequest;
import org.example.exception.address.AddressNotFoundException;
import org.example.exception.address.UnauthorizedAddressAccessException;
import org.example.exception.user.UserNotFoundException;

import java.util.Set;
import java.util.UUID;

/**
 * Интерфейс сервиса для управления адресами пользователей.
 * Предоставляет методы для создания, получения, обновления и удаления адресов.
 */
public interface AddressService {

    /**
     * Добавление нового адреса для пользователя.
     * Если это первый адрес пользователя или указан флаг isDefault, адрес устанавливается как адрес по умолчанию.
            *
            * @param userId Уникальный идентификатор пользователя
     * @param request Данные для создания адреса
     * @return Информация о созданном адресе
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     */
    AddressResponse addAddress(UUID userId, AddressCreateRequest request);

    /**
     * Получение адреса по ID для определенного пользователя.
     *
     * @param userId Уникальный идентификатор пользователя
     * @param addressId Уникальный идентификатор адреса
     * @return Информация об адресе
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     * @throws AddressNotFoundException если адрес с указанным ID не найден
     * @throws UnauthorizedAddressAccessException если адрес не принадлежит указанному пользователю
     */
    AddressResponse getAddressById(UUID userId, UUID addressId);

    /**
     * Получение всех адресов пользователя.
     *
     * @param userId Уникальный идентификатор пользователя
     * @return Набор адресов пользователя
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     */
    Set<AddressResponse> getUserAddresses(UUID userId);

    /**
     * Обновление адреса.
     * Если указан флаг isDefault, адрес устанавливается как адрес по умолчанию.
     *
     * @param userId Уникальный идентификатор пользователя
     * @param addressId Уникальный идентификатор адреса
     * @param request Данные для обновления адреса
     * @return Обновленная информация об адресе
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     * @throws AddressNotFoundException если адрес с указанным ID не найден
     * @throws UnauthorizedAddressAccessException если адрес не принадлежит указанному пользователю
     */
    AddressResponse updateAddress(UUID userId, UUID addressId, AddressUpdateRequest request);

    /**
     * Удаление адреса.
     * Если удаляется адрес по умолчанию, то другой адрес пользователя (если есть) устанавливается как адрес по умолчанию.
     *
     * @param userId Уникальный идентификатор пользователя
     * @param addressId Уникальный идентификатор адреса
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     * @throws AddressNotFoundException если адрес с указанным ID не найден
     * @throws UnauthorizedAddressAccessException если адрес не принадлежит указанному пользователю
     */
    void deleteAddress(UUID userId, UUID addressId);

    /**
     * Установка адреса как адреса по умолчанию для пользователя.
     * Если у пользователя уже есть адрес по умолчанию, он теряет этот статус.
     *
     * @param userId Уникальный идентификатор пользователя
     * @param addressId Уникальный идентификатор адреса
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     * @throws AddressNotFoundException если адрес с указанным ID не найден
     * @throws UnauthorizedAddressAccessException если адрес не принадлежит указанному пользователю
     */
    void setAsDefaultAddress(UUID userId, UUID addressId);

}