package org.example.service.impl;

import feign.FeignException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.example.data.dto.LoginRequest;
import org.example.data.dto.LoginResponse;
import org.example.data.dto.feign.UserCreateDto;
import org.example.data.dto.feign.UserResponse;
import org.example.data.dto.feign.UserUpdateDto;
import org.example.exception.*;
import org.example.feign.UserServiceClient;
import org.example.data.dto.RegisterRequest;
import org.example.service.AuthService;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Реализация интерфейса сервиса аутентификации и управления пользователями.
 * Обеспечивает взаимодействие с Keycloak и User Service для выполнения операций с пользователями.
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private Keycloak keycloak;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${manager.secret-code}")
    private String managerSecret;

    @Value("${admin.secret-code}")
    private String adminSecret;

    /**
     * {@inheritDoc}
     * @throws UserAlreadyExistsException если пользователь с таким email уже существует
     * @throws RegistrationException если произошла ошибка при регистрации
     */
    @Override
    public ResponseEntity<String> register(RegisterRequest request) {
        log.debug("Регистрация пользователя с email: {}", request.email());

        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(request.email());
        userRepresentation.setEmail(request.email());
        userRepresentation.setFirstName(request.firstName());
        userRepresentation.setLastName(request.lastName());
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.password());
        credential.setTemporary(false);
        userRepresentation.setCredentials(Collections.singletonList(credential));

        String userId = null;
        try (Response response = usersResource.create(userRepresentation)) {
            int status = response.getStatus();
            if (status == 201) {
                log.debug("Пользователь с username: {} успешно зарегистрирован в Keycloak", request.email());
                userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

                UserResource userResource = usersResource.get(userId);

                RoleScopeResource roleScopeResource = userResource.roles().realmLevel();
                List<RoleRepresentation> availableRoles = roleScopeResource.listAvailable();

                String userRole;
                if (request.password().equals(managerSecret)) {
                    userRole = "MANAGER";
                } else if (request.password().equals(adminSecret)) {
                    userRole = "ADMIN";
                } else {
                    userRole = "USER";
                }

                List<RoleRepresentation> rolesToAdd = new ArrayList<>();
                availableRoles.stream()
                        .filter(role -> role.getName().equals(userRole))
                        .findFirst()
                        .ifPresent(rolesToAdd::add);
                roleScopeResource.add(rolesToAdd);

                UserCreateDto userDto = new UserCreateDto(
                        request.firstName(),
                        request.lastName(),
                        request.email(),
                        request.phone()
                );
                log.debug("Отправка данных о пользователе в USER-SERVICE: {}", userDto);
                try {
                    ResponseEntity<UserResponse> userResponse = userServiceClient.createUser(userDto);
                    if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
                        log.error("Не удалось создать пользователя в USER-SERVICE status: {}", userResponse.getStatusCode());
                        throw new RegistrationException("Не удалось создать пользователя в USER-SERVICE");
                    }
                    log.debug("Пользователь успешно создан в USER-SERVICE с id: {}", userResponse.getBody().id());
                    return ResponseEntity.status(HttpStatus.CREATED).body("Пользователь успешно создан");
                } catch (FeignException e) {
                    log.error("Ошибка при вызове USER-SERVICE: status: {}, body: {}", e.status(), e.getMessage());
                    throw new RegistrationException("Ошибка в USER-SERVICE: " + e.getMessage(), e);
                }
            } else if (status == 409) {
                log.error("Пользователь с email: {} уже зарегистрирован", request.email());
                throw new UserAlreadyExistsException(String.format("Пользователь с email: %s уже зарегистрирован", request.email()));
            } else {
                log.error("Ошибка регистрации в Keycloak: status={}", status);
                throw new RegistrationException("Не удалось зарегистрировать пользователя в Keycloak");
            }
        } catch (Exception e) {
            if (userId != null) {
                log.debug("Откат: удаление пользователя с id: {} из Keycloak", userId);
                usersResource.get(userId).remove();
            }
            if (e instanceof UserAlreadyExistsException || e instanceof RegistrationException) {
                throw e;
            }
            log.error("Ошибка при регистрации: email={}, error={}", request.email(), e.getMessage(), e);
            throw new RegistrationException("Ошибка регистрации", e);
        }
    }

    /**
     * {@inheritDoc}
     * @throws InvalidCredentialsException если учетные данные неверны
     * @throws LoginException если произошла ошибка при входе
     */
    @Override
    public ResponseEntity<LoginResponse> login(LoginRequest request) {
        log.debug("Вход пользователя с username: {}", request.email());

        try {
            String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("grant_type", "password");
            body.add("username", request.email());
            body.add("password", request.password());

            HttpEntity<MultiValueMap<String, String>> httpRequest = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, httpRequest, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("Пользователь с username: {} успешно вошел", request.email());
                Map<String, Object> tokenResponse = response.getBody();
                return ResponseEntity.ok().body(new LoginResponse(
                        (String) tokenResponse.get("access_token"),
                        (String) tokenResponse.get("refresh_token"),
                        Long.valueOf(tokenResponse.get("expires_in").toString()),
                        (String) tokenResponse.get("token_type")
                ));
            } else {
                log.error("Неожиданный ответ от Keycloak: status={}", response.getStatusCode());
                throw new LoginException("Не удалось выполнить вход");
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.error("Неверные учетные данные для пользователя: {}", request.email());
                throw new InvalidCredentialsException("Неверные учетные данные");
            } else {
                log.error("Ошибка при попытке входа: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
                throw new LoginException("Ошибка входа: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Неизвестная ошибка при входе: {}", e.getMessage(), e);
            throw new LoginException("Ошибка входа", e);
        }
    }

    /**
     * {@inheritDoc}
     * @throws UserNotFoundException если пользователь не найден
     */
    @Override
    public ResponseEntity<String> updateUserInKeycloak(String email, UserUpdateDto userUpdateDto) {
        log.debug("Обновление данных пользователя с username: {}", email);

        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            List<UserRepresentation> users = usersResource.searchByUsername(email, true);
            if (users.isEmpty()) {
                log.error("Пользователь с email: {} не найден в Keycloak", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
            }

            UserRepresentation userRepresentation = users.get(0);
            String userId = userRepresentation.getId();

            userRepresentation.setFirstName(userUpdateDto.firstName());
            userRepresentation.setLastName(userUpdateDto.lastName());

            usersResource.get(userId).update(userRepresentation);

            log.debug("Пользователь с id: {} успешно обновлен в Keycloak", userId);
            return ResponseEntity.ok("Данные пользователя успешно обновлены");
        } catch (Exception e) {
            log.error("Ошибка при обновлении пользователя в Keycloak: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка обновления данных пользователя");
        }
    }

    /**
     * {@inheritDoc}
     * @throws UserNotFoundException если пользователь не найден
     */
    @Override
    public ResponseEntity<String> deleteUserFromKeycloak(String email) {
        log.debug("Удаление пользователя из Keycloak с email: {}", email);

        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            List<UserRepresentation> users = usersResource.searchByUsername(email, true);
            if (users.isEmpty()) {
                log.error("Пользователь с email: {} не найден в Keycloak", email);
                throw new UserNotFoundException("Пользователь не найден в Keycloak");
            }

            UserRepresentation userRepresentation = users.get(0);
            String userId = userRepresentation.getId();

            usersResource.get(userId).remove();

            log.debug("Пользователь с id: {}, email: {} успешно удален из Keycloak", userId, email);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            if (e instanceof UserNotFoundException) {
                throw e;
            }
            log.error("Ошибка при удалении пользователя из Keycloak: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка удаления пользователя из Keycloak");
        }
    }

}
