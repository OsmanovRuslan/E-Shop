package org.example.service.impl;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;
    private UserEntity userEntity;
    private UserEntity updatedUserEntity;
    private UserResponse userResponse;
    private UserResponse updatedUserResponse;
    private UserUpdateDto userUpdateDto;
    private UserDetailResponse userDetailResponse;
    private UserCreateRequest createRequest;
    private UserUpdateRequest updateRequest;
    private ResponseEntity<String> keycloakResponse;
    private ResponseEntity<String> keycloakErrorResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setFirstName("Василий");
        userEntity.setLastName("Лопатин");
        userEntity.setEmail("vasya.lopa@example.com");
        userEntity.setPhone("+12345678901");
        userEntity.setCreatedAt(LocalDateTime.now());
        userEntity.setUpdatedAt(LocalDateTime.now());


        updatedUserEntity = new UserEntity();
        updatedUserEntity.setId(userId);
        updatedUserEntity.setFirstName("Василий");
        updatedUserEntity.setLastName("Тонкин");
        updatedUserEntity.setEmail("vasya.lopa@example.com");
        updatedUserEntity.setPhone("+19876543210");
        updatedUserEntity.setCreatedAt(LocalDateTime.now());
        updatedUserEntity.setUpdatedAt(LocalDateTime.now());

        userResponse = new UserResponse(
                userId,
                "Василий",
                "Лопатин",
                "vasya.lopa@example.com",
                "+12345678901"
        );

        updatedUserResponse = new UserResponse(
                userId,
                "Василий",
                "Тонкин",
                "vasya.lopa@example.com",
                "+19876543210"
        );

        userDetailResponse = new UserDetailResponse(
                userId,
                "Василий",
                "Лопатин",
                "vasya.lopa@example.com",
                "+12345678901",
                Set.of()
        );

        createRequest = new UserCreateRequest(
                "Василий",
                "Лопатин",
                "vasya.lopa@example.com",
                "+12345678901"
        );

        updateRequest = new UserUpdateRequest(
                "Василий",
                "Тонкин",
                "+19876543210"
        );

        userUpdateDto = new UserUpdateDto("Василий", "Тонкин");

        keycloakResponse = ResponseEntity.ok("Success");
        keycloakErrorResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
    }

    @Test
    void createUser_ShouldReturnUserResponse() {
        when(userMapper.toUserEntity(createRequest)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.toUserResponse(userEntity)).thenReturn(userResponse);

        UserResponse result = userService.createUser(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);

        verify(userMapper).toUserEntity(any(UserCreateRequest.class));
        verify(userRepository).save(any(UserEntity.class));
        verify(userMapper).toUserResponse(any(UserEntity.class));
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUserResponse() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toUserResponse(userEntity)).thenReturn(userResponse);

        UserResponse result = userService.getUserById(userId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);

        verify(userRepository).findById(userId);
        verify(userMapper).toUserResponse(any(UserEntity.class));
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId));

        verify(userRepository).findById(userId);
        verify(userMapper, never()).toUserResponse(any(UserEntity.class));
    }

    @Test
    void getAllUsers_ShouldReturnUserResponseList() {
        List<UserEntity> userEntityList = List.of(userEntity, userEntity);

        when(userRepository.findAll()).thenReturn(userEntityList);
        when(userMapper.toUserResponseList(userEntityList)).thenReturn(List.of(userResponse, userResponse));

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).isNotNull();
        assertEquals(2, result.size());

        verify(userRepository).findAll();
        verify(userMapper).toUserResponseList(anyList());
    }

    @Test
    void getUserDetailsById_ShouldReturnUserDetailResponse() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toUserDetailResponse(userEntity)).thenReturn(userDetailResponse);

        UserDetailResponse result = userService.getUserDetailsById(userId);

        assertThat(result).isNotNull();
        assertEquals(userId, result.id());

        verify(userRepository).findById(userId);
        verify(userMapper).toUserDetailResponse(any(UserEntity.class));
    }

    @Test
    void getUserDetailsById_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserDetailsById(userId));

        verify(userRepository).findById(userId);
        verify(userMapper, never()).toUserDetailResponse(any(UserEntity.class));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUserResponse() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(updatedUserEntity)).thenReturn(updatedUserEntity);
        when(userMapper.toUserUpdateDto(updatedUserEntity)).thenReturn(userUpdateDto);
        when(authServiceClient.updateUserInKeycloak(updatedUserEntity.getEmail(), userUpdateDto)).thenReturn(keycloakResponse);
        when(userMapper.toUserResponse(updatedUserEntity)).thenReturn(updatedUserResponse);

        UserResponse result = userService.updateUser(userId, updateRequest);

        assertThat(result).isNotNull();
        assertEquals(userId, result.id());

        verify(userRepository).findById(userId);
        verify(userMapper).updateEntityFromRequest(any(UserUpdateRequest.class), any(UserEntity.class));
        verify(userRepository).save(any(UserEntity.class));
        verify(userMapper).toUserUpdateDto(any(UserEntity.class));
        verify(authServiceClient).updateUserInKeycloak(anyString(), any(UserUpdateDto.class));
        verify(userMapper).toUserUpdateDto(any(UserEntity.class));
        verify(userMapper).toUserResponse(any(UserEntity.class));
    }

    @Test
    void updateUser_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(userId, updateRequest));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(authServiceClient, never()).updateUserInKeycloak(anyString(), any(UserUpdateDto.class));
    }

    @Test
    void updateUser_WhenKeycloakUpdateFails_ShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(userEntity)).thenReturn(updatedUserEntity);
        when(userMapper.toUserUpdateDto(updatedUserEntity)).thenReturn(userUpdateDto);
        when(authServiceClient.updateUserInKeycloak(updatedUserEntity.getEmail(), userUpdateDto)).thenReturn(keycloakErrorResponse);

        Exception exception = assertThrows(RuntimeException.class, () ->
                userService.updateUser(userId, updateRequest));

        assertTrue(exception.getMessage().contains("Не удалось обновить пользователя в Keycloak"));

        verify(userRepository).findById(userId);
        verify(userMapper).updateEntityFromRequest(any(UserUpdateRequest.class), any(UserEntity.class));
        verify(userRepository).save(any(UserEntity.class));
        verify(userMapper).toUserUpdateDto(any(UserEntity.class));
        verify(authServiceClient).updateUserInKeycloak(anyString(), any(UserUpdateDto.class));
    }

    @Test
    void deleteUser_ShouldBeSuccess() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(authServiceClient.deleteUserFromKeycloak(anyString())).thenReturn(keycloakResponse);

        userService.deleteUser(userId);

        verify(userRepository).findById(userId);
        verify(userRepository).delete(any(UserEntity.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).delete(any(UserEntity.class));
        verify(authServiceClient, never()).deleteUserFromKeycloak(anyString());
    }

    @Test
    void deleteUser_WhenKeycloakDeleteFails_ShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(authServiceClient.deleteUserFromKeycloak(userEntity.getEmail())).thenReturn(keycloakErrorResponse);

        Exception exception = assertThrows(RuntimeException.class, () ->
                userService.deleteUser(userId));

        assertTrue(exception.getMessage().contains("Ошибка при удалении пользователя из Keycloak. Транзакция будет отменена, пользователь останется в БД."));

        verify(userRepository).findById(userId);
        verify(userRepository).delete(any(UserEntity.class));
        verify(authServiceClient).deleteUserFromKeycloak(anyString());
    }

}