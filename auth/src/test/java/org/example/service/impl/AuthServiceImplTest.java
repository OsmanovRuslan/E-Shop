package org.example.service.impl;

import feign.FeignException;
import org.example.data.dto.LoginRequest;
import org.example.data.dto.LoginResponse;
import org.example.data.dto.RegisterRequest;
import org.example.data.dto.feign.UserCreateDto;
import org.example.data.dto.feign.UserResponse;
import org.example.data.dto.feign.UserUpdateDto;
import org.example.exception.InvalidCredentialsException;
import org.example.exception.LoginException;
import org.example.exception.RegistrationException;
import org.example.exception.UserAlreadyExistsException;
import org.example.exception.UserNotFoundException;
import org.example.feign.UserServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @Mock
    private RolesResource rolesResource;

    @Mock
    private RoleMappingResource roleMappingResource;

    @Mock
    private RoleScopeResource roleScopeResource;

    @Mock
    private Response response;

    @InjectMocks
    private AuthServiceImpl authService;

    private final String TEST_EMAIL = "user@example.com";
    private final String TEST_PASSWORD = "password123";
    private final String TEST_FIRST_NAME = "Вячеслав";
    private final String TEST_LAST_NAME = "Тонкин";
    private final String TEST_PHONE = "+1234567890";
    private final UUID TEST_USER_ID = UUID.randomUUID();
    private final String KEYCLOAK_USER_ID = "keycloak-user-id";
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private UserUpdateDto userUpdateDto;
    private UserRepresentation userRepresentation;
    private Map<String, Object> tokenResponse;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "realm", "test-realm");
        ReflectionTestUtils.setField(authService, "clientId", "test-client");
        ReflectionTestUtils.setField(authService, "clientSecret", "test-secret");
        ReflectionTestUtils.setField(authService, "serverUrl", "http://localhost:8080/auth");
        ReflectionTestUtils.setField(authService, "managerSecret", "manager-secret");
        ReflectionTestUtils.setField(authService, "adminSecret", "admin-secret");

        registerRequest = new RegisterRequest(
                TEST_EMAIL,
                TEST_PASSWORD,
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                TEST_PHONE
        );

        loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

        userUpdateDto = new UserUpdateDto(
                "Updated First Name",
                "Updated Last Name"
        );

        userRepresentation = new UserRepresentation();
        userRepresentation.setId(KEYCLOAK_USER_ID);
        userRepresentation.setUsername(TEST_EMAIL);
        userRepresentation.setEmail(TEST_EMAIL);
        userRepresentation.setFirstName(TEST_FIRST_NAME);
        userRepresentation.setLastName(TEST_LAST_NAME);

        tokenResponse = new HashMap<>();
        tokenResponse.put("access_token", "test-access-token");
        tokenResponse.put("refresh_token", "test-refresh-token");
        tokenResponse.put("expires_in", 300);
        tokenResponse.put("token_type", "Bearer");
    }

    @Test
    void register_ShouldReturnCreated() throws Exception {
        UserResponse userResponse = new UserResponse(
                TEST_USER_ID,
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                TEST_EMAIL,
                TEST_PHONE
        );
        List<RoleRepresentation> availableRoles = new ArrayList<>();
        RoleRepresentation userRole = new RoleRepresentation();
        userRole.setName("USER");
        availableRoles.add(userRole);

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        when(response.getLocation()).thenReturn(new URI("http://localhost:8080/auth/admin/realms/test-realm/users/" + KEYCLOAK_USER_ID));
        when(usersResource.get(KEYCLOAK_USER_ID)).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(userResource.roles().realmLevel()).thenReturn(roleScopeResource);
        when(roleScopeResource.listAvailable()).thenReturn(availableRoles);
        doNothing().when(roleScopeResource).add(anyList());
        when(userServiceClient.createUser(any(UserCreateDto.class))).thenReturn(ResponseEntity.ok(userResponse));

        ResponseEntity<String> result = authService.register(registerRequest);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals("Пользователь успешно создан", result.getBody());

        verify(usersResource).create(any(UserRepresentation.class));
        verify(usersResource).get(KEYCLOAK_USER_ID);
        verify(roleScopeResource).listAvailable();
        verify(roleScopeResource).add(anyList());
        verify(userServiceClient).createUser(any(UserCreateDto.class));
    }

    @Test
    void register_ShouldReturnUserAlreadyExists() {
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(409);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerRequest));

        verify(usersResource).create(any(UserRepresentation.class));
        verifyNoInteractions(userServiceClient);
    }

    @Test
    void register_ShouldFailedRegistrationInKeycloak() {
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(400);

        assertThrows(RegistrationException.class, () -> authService.register(registerRequest));

        verify(usersResource).create(any(UserRepresentation.class));
        verifyNoInteractions(userServiceClient);
    }

    @Test
    void register_ShouldReturnUserServiceError() throws Exception {
        List<RoleRepresentation> availableRoles = new ArrayList<>();
        RoleRepresentation userRole = new RoleRepresentation();
        userRole.setName("USER");
        availableRoles.add(userRole);

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        when(response.getLocation()).thenReturn(new URI("http://localhost:8080/auth/admin/realms/test-realm/users/" + KEYCLOAK_USER_ID));
        when(usersResource.get(KEYCLOAK_USER_ID)).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(userResource.roles().realmLevel()).thenReturn(roleScopeResource);
        when(roleScopeResource.listAvailable()).thenReturn(availableRoles);
        doNothing().when(roleScopeResource).add(anyList());
        when(userServiceClient.createUser(any(UserCreateDto.class))).thenThrow(FeignException.class);

        assertThrows(RegistrationException.class, () -> authService.register(registerRequest));

        verify(usersResource).create(any(UserRepresentation.class));
        verify(userServiceClient).createUser(any(UserCreateDto.class));
        verify(usersResource, times(2)).get(KEYCLOAK_USER_ID);
        verify(userResource).remove();
    }

    @Test
    void login_ShouldReturnOk() {
        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(ResponseEntity.ok(tokenResponse));

        ResponseEntity<LoginResponse> result = authService.login(loginRequest);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("test-access-token", result.getBody().accessToken());
        assertEquals("test-refresh-token", result.getBody().refreshToken());
        assertEquals(300L, result.getBody().expiresIn());
        assertEquals("Bearer", result.getBody().tokenType());

        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void login_ShouldReturnInvalidCredentials() {
        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));

        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void login_ShouldReturnLoginException() {
        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad request"));

        assertThrows(LoginException.class, () -> authService.login(loginRequest));

        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void updateUserInKeycloak_ShouldReturnOk() {
        List<UserRepresentation> users = Collections.singletonList(userRepresentation);
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.searchByUsername(TEST_EMAIL, true)).thenReturn(users);
        when(usersResource.get(KEYCLOAK_USER_ID)).thenReturn(userResource);
        doNothing().when(userResource).update(any(UserRepresentation.class));

        ResponseEntity<String> result = authService.updateUserInKeycloak(TEST_EMAIL, userUpdateDto);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Данные пользователя успешно обновлены", result.getBody());

        verify(usersResource).searchByUsername(TEST_EMAIL, true);
        verify(usersResource).get(KEYCLOAK_USER_ID);
        verify(userResource).update(any(UserRepresentation.class));
    }

    @Test
    void updateUserInKeycloak_ShouldReturnUserNotFound() {
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.searchByUsername(TEST_EMAIL, true)).thenReturn(Collections.emptyList());

        ResponseEntity<String> result = authService.updateUserInKeycloak(TEST_EMAIL, userUpdateDto);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Пользователь не найден", result.getBody());

        verify(usersResource).searchByUsername(TEST_EMAIL, true);
        verifyNoInteractions(userResource);
    }

    @Test
    void deleteUserFromKeycloak_ShouldReturnOk() {
        List<UserRepresentation> users = Collections.singletonList(userRepresentation);
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.searchByUsername(TEST_EMAIL, true)).thenReturn(users);
        when(usersResource.get(KEYCLOAK_USER_ID)).thenReturn(userResource);
        doNothing().when(userResource).remove();

        ResponseEntity<String> result = authService.deleteUserFromKeycloak(TEST_EMAIL);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

        verify(usersResource).searchByUsername(TEST_EMAIL, true);
        verify(usersResource).get(KEYCLOAK_USER_ID);
        verify(userResource).remove();
    }

    @Test
    void deleteUserFromKeycloak_UserNotFound() {
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.searchByUsername(TEST_EMAIL, true)).thenReturn(Collections.emptyList());

        assertThrows(UserNotFoundException.class, () -> authService.deleteUserFromKeycloak(TEST_EMAIL));

        verify(usersResource).searchByUsername(TEST_EMAIL, true);
        verifyNoInteractions(userResource);
    }
}