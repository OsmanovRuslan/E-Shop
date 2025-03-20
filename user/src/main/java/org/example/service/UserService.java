package org.example.service;

import org.example.data.dto.user.UserCreateRequest;
import org.example.data.dto.user.UserDetailResponse;
import org.example.data.dto.user.UserResponse;
import org.example.data.dto.user.UserUpdateRequest;
import org.example.exception.user.UserNotFoundException;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс сервиса для управления пользователями.
 * Предоставляет методы для создания, получения, обновления и удаления пользователей.
 */
public interface UserService {

    /**
     * Создание нового пользователя.
     *
     * @param request Данные для создания пользователя
     * @return Информация о созданном пользователе
     * @throws IllegalArgumentException если запрос содержит некорректные данные
     */
    UserResponse createUser(UserCreateRequest request);

    /**
     * Получение пользователя по ID.
     *
     * @param userId Уникальный идентификатор пользователя
     * @return Информация о пользователе
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     */
    UserResponse getUserById(UUID userId);

    /**
     * Получение списка всех пользователей.
     *
     * @return Список пользователей
     */
    List<UserResponse> getAllUsers();

    /**
     * Получение детальной информации о пользователе по ID.
     * Включает информацию об адресах пользователя.
     *
     * @param userId Уникальный идентификатор пользователя
     * @return Детальная информация о пользователе
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     */
    UserDetailResponse getUserDetailsById(UUID userId);

    /**
     * Обновление пользователя.
     * Также отправляет запрос на обновление пользователя в Keycloak через AuthServiceClient.
     *
     * @param userId Уникальный идентификатор пользователя
     * @param request Данные для обновления пользователя
     * @return Обновленная информация о пользователе
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     * @throws RuntimeException если не удалось обновить пользователя в Keycloak
     */
    UserResponse updateUser(UUID userId, UserUpdateRequest request);

    /**
     * Удаление пользователя.
     * Также отправляет запрос на удаление пользователя в Keycloak через AuthServiceClient.
     *
     * @param userId Уникальный идентификатор пользователя
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     * @throws RuntimeException если не удалось удалить пользователя из Keycloak
     */
    void deleteUser(UUID userId);

}