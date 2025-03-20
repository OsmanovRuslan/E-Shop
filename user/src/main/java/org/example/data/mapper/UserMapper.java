package org.example.data.mapper;

import org.example.data.dto.feign.UserUpdateDto;
import org.example.data.dto.user.UserCreateRequest;
import org.example.data.dto.user.UserDetailResponse;
import org.example.data.dto.user.UserResponse;
import org.example.data.dto.user.UserUpdateRequest;
import org.example.data.entity.UserEntity;
import org.mapstruct.*;

import java.util.List;

/**
 * Интерфейс маппера для преобразования между DTO и сущностями пользователя.
 * Использует MapStruct для автоматической генерации реализации.
 */
@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface UserMapper {

    /**
     * Преобразование DTO запроса на создание пользователя в сущность пользователя.
     *
     * @param request DTO запроса на создание пользователя
     * @return Сущность пользователя
     */
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    UserEntity toUserEntity(UserCreateRequest request);

    /**
     * Преобразование сущности пользователя в DTO ответа о пользователе.
     *
     * @param userEntity Сущность пользователя
     * @return DTO ответа о пользователе
     */
    UserResponse toUserResponse(UserEntity userEntity);

    /**
     * Преобразование сущности пользователя в DTO детального ответа о пользователе.
     * Включает информацию об адресах пользователя.
     *
     * @param userEntity Сущность пользователя
     * @return DTO детального ответа о пользователе
     */
    UserDetailResponse toUserDetailResponse(UserEntity userEntity);

    /**
     * Обновление сущности пользователя из DTO запроса на обновление.
     *
     * @param request DTO запроса на обновление пользователя
     * @param entity Сущность пользователя для обновления
     */
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    void updateEntityFromRequest(UserUpdateRequest request, @MappingTarget UserEntity entity);

    /**
     * Преобразование сущности пользователя в DTO для обновления пользователя в Keycloak.
     *
     * @param userEntity Сущность пользователя
     * @return DTO для обновления пользователя в Keycloak
     */
    UserUpdateDto toUserUpdateDto(UserEntity userEntity);

    /**
     * Преобразование списка сущностей пользователей в список DTO ответов о пользователях.
     *
     * @param userEntities Список сущностей пользователей
     * @return Список DTO ответов о пользователях
     */
    List<UserResponse> toUserResponseList(List<UserEntity> userEntities);
}
