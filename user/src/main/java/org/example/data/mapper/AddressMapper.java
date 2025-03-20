package org.example.data.mapper;

import org.example.data.dto.address.AddressCreateRequest;
import org.example.data.dto.address.AddressResponse;
import org.example.data.dto.address.AddressUpdateRequest;
import org.example.data.entity.AddressEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Интерфейс маппера для преобразования между DTO и сущностями адреса.
 * Использует MapStruct для автоматической генерации реализации.
 */
@Mapper(componentModel = "spring")
public interface AddressMapper {

    /**
     * Преобразование сущности адреса в DTO ответа об адресе.
     *
     * @param addressEntity Сущность адреса
     * @return DTO ответа об адресе
     */
    AddressResponse toAddressResponse(AddressEntity addressEntity);

    /**
     * Преобразование DTO запроса на создание адреса в сущность адреса.
     *
     * @param createRequest DTO запроса на создание адреса
     * @return Сущность адреса
     */
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    AddressEntity toAddressEntity(AddressCreateRequest createRequest);

    /**
     * Обновление сущности адреса из DTO запроса на обновление.
     *
     * @param updateRequest DTO запроса на обновление адреса
     * @param addressEntity Сущность адреса для обновления
     */
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateAddressFromDto(AddressUpdateRequest updateRequest, @MappingTarget AddressEntity addressEntity);

}
