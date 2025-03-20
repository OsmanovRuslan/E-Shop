package org.example.service;

import org.example.data.dto.LoginRequest;
import org.example.data.dto.LoginResponse;
import org.example.data.dto.RegisterRequest;
import org.example.data.dto.feign.UserUpdateDto;
import org.example.exception.*;
import org.springframework.http.ResponseEntity;

/**
 * Интерфейс сервиса аутентификации и управления пользователями.
 * Определяет методы для регистрации, входа, обновления и удаления пользователей через KeyCloak.
 */
public interface AuthService {

    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param request Данные для регистрации пользователя
     * @return ResponseEntity с сообщением о результате операции
     * @throws UserAlreadyExistsException если пользователь с таким email уже существует
     * @throws RegistrationException если произошла ошибка при регистрации
     */
    ResponseEntity<String> register(RegisterRequest request);

    /**
     * Аутентифицирует пользователя и предоставляет токены доступа.
     *
     * @param request Данные для входа пользователя (email и пароль)
     * @return ResponseEntity с токенами доступа и обновления
     * @throws InvalidCredentialsException если учетные данные неверны
     * @throws LoginException если произошла ошибка при входе
     */
    ResponseEntity<LoginResponse> login(LoginRequest request);

    /**
     * Обновляет информацию о пользователе в Keycloak.
     *
     * @param email Email пользователя, данные которого нужно обновить
     * @param userUpdateDto Новые данные пользователя
     * @return ResponseEntity с сообщением о результате операции
     * @throws UserNotFoundException если пользователь не найден
     */
    ResponseEntity<String> updateUserInKeycloak(String email, UserUpdateDto userUpdateDto);

    /**
     * Удаляет пользователя из Keycloak.
     *
     * @param email Email пользователя, которого нужно удалить
     * @return ResponseEntity с сообщением о результате операции
     * @throws UserNotFoundException если пользователь не найден
     */
    ResponseEntity<String> deleteUserFromKeycloak(String email);
}
