package org.example.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.data.dto.feign.UserRestoreDto;
import org.example.data.dto.feign.UserUpdateDto;
import org.example.data.dto.user.UserCreateRequest;
import org.example.data.dto.user.UserDetailResponse;
import org.example.data.dto.user.UserResponse;
import org.example.data.dto.user.UserUpdateRequest;
import org.example.data.entity.UserEntity;
import org.example.data.mapper.UserMapper;
import org.example.exception.user.UserNotFoundException;
import org.example.feign.AuthServiceClient;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Реализация интерфейса сервиса для управления пользователями.
 * Обеспечивает бизнес-логику для операций с пользователями и интеграцию с Keycloak.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthServiceClient authServiceClient;

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException если запрос содержит некорректные данные
     */
    @Override
    public UserResponse createUser(UserCreateRequest request) {
        log.debug("Создание нового пользователя с email: {}", request.email());

        UserEntity userEntity = userMapper.toUserEntity(request);
        userEntity = userRepository.save(userEntity);

        log.debug("Создан пользователь с id: {}", userEntity.getId());
        return userMapper.toUserResponse(userEntity);
    }

    /**
     * {@inheritDoc}
     *
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     */
    @Override
    public UserResponse getUserById(UUID userId) {
        log.debug("Получение пользователя с id: {}", userId);

        UserEntity userEntity = getUserOrThrow(userId);

        log.debug("Получен пользователь с id: {}", userEntity.getId());
        return userMapper.toUserResponse(userEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserResponse> getAllUsers() {
        log.debug("Получение списка пользователей");
        List<UserEntity> users = userRepository.findAll();
        return userMapper.toUserResponseList(users);
    }

    /**
     * {@inheritDoc}
     *
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     */
    @Override
    public UserDetailResponse getUserDetailsById(UUID userId) {
        log.debug("Получение детальной информации пользователя с id: {}", userId);

        UserEntity userEntity = getUserOrThrow(userId);

        log.debug("Получена детальная информация пользователя с id: {}", userEntity.getId());
        return userMapper.toUserDetailResponse(userEntity);
    }

    /**
     * {@inheritDoc}
     *
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     * @throws RuntimeException если не удалось обновить пользователя в Keycloak
     */
    @Override
    public UserResponse updateUser(UUID userId, UserUpdateRequest request) {
        log.debug("Обновление пользователя с id: {}", userId);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.format("Пользователь с id: %s не найден", userId)));

        userMapper.updateEntityFromRequest(request, userEntity);
        userEntity.setUpdatedAt(LocalDateTime.now());

        UserEntity updatedUser = userRepository.save(userEntity);

        UserUpdateDto userUpdateDto = userMapper.toUserUpdateDto(updatedUser);

        ResponseEntity<String> keycloakResponse = authServiceClient.updateUserInKeycloak(
                updatedUser.getEmail(), userUpdateDto);

        if (!keycloakResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Не удалось обновить пользователя в Keycloak: {}", keycloakResponse.getBody());
            throw new RuntimeException("Не удалось обновить пользователя в Keycloak");
        }

        log.debug("Пользователь успешно обновлен в Keycloak");
        return userMapper.toUserResponse(updatedUser);
    }

    /**
     * {@inheritDoc}
     *
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     * @throws RuntimeException если не удалось удалить пользователя из Keycloak
     */
    @Override
    public void deleteUser(UUID userId) {
        log.debug("Удаление пользователя с id: {}", userId);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден с id: " + userId));

        String userEmail = userEntity.getEmail();

        userRepository.delete(userEntity);
        log.debug("Пользователь отмечен для удаления из БД, попытка удаления из Keycloak...");

        try {
            ResponseEntity<String> keycloakResponse = authServiceClient.deleteUserFromKeycloak(userEmail);

            if (!keycloakResponse.getStatusCode().is2xxSuccessful()) {
                log.error("Не удалось удалить пользователя из Keycloak: {}", keycloakResponse.getBody());
                throw new RuntimeException("Не удалось удалить пользователя из Keycloak");
            }

            log.debug("Пользователь с id: {} успешно удален из Keycloak", userId);
        } catch (Exception e) {
            log.error("Ошибка при удалении пользователя с id: {} из Keycloak: {}", userId, e.getMessage());
            throw new RuntimeException("Ошибка при удалении пользователя из Keycloak. Транзакция будет отменена, пользователь останется в БД.");
        }
    }

    /**
     * Вспомогательный метод для получения пользователя по ID или выброса исключения.
     *
     * @param userId Уникальный идентификатор пользователя
     * @return Сущность пользователя
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     */
    private UserEntity getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id: {} не найден", userId);
                    return new UserNotFoundException(String.format("Пользователь с id: %s не найден", userId));
                });
    }

}
