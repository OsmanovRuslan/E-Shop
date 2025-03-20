package org.example.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.data.dto.address.AddressCreateRequest;
import org.example.data.dto.address.AddressResponse;
import org.example.data.dto.address.AddressUpdateRequest;
import org.example.data.entity.AddressEntity;
import org.example.data.entity.UserEntity;
import org.example.data.mapper.AddressMapper;
import org.example.exception.address.AddressNotFoundException;
import org.example.exception.address.UnauthorizedAddressAccessException;
import org.example.exception.user.UserNotFoundException;
import org.example.repository.AddressRepository;
import org.example.repository.UserRepository;
import org.example.service.AddressService;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Реализация интерфейса сервиса для управления адресами пользователей.
 * Обеспечивает бизнес-логику для операций с адресами.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    /**
     * {@inheritDoc}
     *
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     */
    @Override
    public AddressResponse addAddress(UUID userId, AddressCreateRequest request) {
        log.debug("Добавление адреса для пользователя с id: {}", userId);

        UserEntity userEntity = getUserOrThrow(userId);

        AddressEntity addressEntity = addressMapper.toAddressEntity(request);
        addressEntity.setUser(userEntity);

        if (request.isDefault() || addressRepository.countByUser_Id(userId) == 0) {
            if (request.isDefault()) {
                addressRepository.findByUser_IdAndIsDefaultTrue(userId)
                        .ifPresent(existingDefault -> {
                            existingDefault.setIsDefault(false);
                            addressRepository.save(existingDefault);
                        });
            }
            addressEntity.setIsDefault(true);
        }

        addressEntity = addressRepository.save(addressEntity);

        log.debug("Добавлен адрес с id: {} для пользователя с id: {}", addressEntity.getId(), userId);
        return addressMapper.toAddressResponse(addressEntity);
    }

    /**
     * {@inheritDoc}
     *
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     * @throws AddressNotFoundException если адрес с указанным ID не найден
     * @throws UnauthorizedAddressAccessException если адрес не принадлежит указанному пользователю
     */
    @Override
    public AddressResponse getAddressById(UUID userId, UUID addressId) {
        log.debug("Получен адрес с id: {} для пользователя с id: {}", addressId, userId);

        AddressEntity addressEntity = getAddressForUserOrThrow(userId, addressId);

        log.debug("Получен адрес с id: {}", addressEntity.getId());
        return addressMapper.toAddressResponse(addressEntity);
    }

    /**
     * {@inheritDoc}
     *
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     */
    @Override
    public Set<AddressResponse> getUserAddresses(UUID userId) {
        log.debug("Получение всех адресов пользователя с id: {}", userId);

        if (!userRepository.existsById(userId)) {
            log.error("Пользователь с id: {} не найден", userId);
            throw new UserNotFoundException(String.format("Пользователь с id: %s не найден", userId));
        }

        Set<AddressEntity> addressEntities = addressRepository.findByUser_Id(userId);

        log.debug("Получено {} адресов для пользователя с id: {}", addressEntities.size(), userId);
        return addressEntities.stream()
                .map(addressMapper::toAddressResponse)
                .collect(Collectors.toSet());
    }

    /**
     * {@inheritDoc}
     *
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     * @throws AddressNotFoundException если адрес с указанным ID не найден
     * @throws UnauthorizedAddressAccessException если адрес не принадлежит указанному пользователю
     */
    @Override
    public AddressResponse updateAddress(UUID userId, UUID addressId, AddressUpdateRequest request) {
        log.debug("Обновление адреса с id: {} для пользователя с id: {}", addressId, userId);

        AddressEntity addressEntity = getAddressForUserOrThrow(userId, addressId);

        addressMapper.updateAddressFromDto(request, addressEntity);

        if (request.isDefault() && !addressEntity.getIsDefault()) {
            addressRepository.findByUser_IdAndIsDefaultTrue(userId)
                    .ifPresent(existingDefault -> {
                        existingDefault.setIsDefault(false);
                        addressRepository.save(existingDefault);
                    });

            addressEntity.setIsDefault(true);
        }

        addressEntity = addressRepository.save(addressEntity);

        log.debug("Обновлен адрес с id: {}", addressEntity.getId());
        return addressMapper.toAddressResponse(addressEntity);
    }

    /**
     * {@inheritDoc}
     *
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     * @throws AddressNotFoundException если адрес с указанным ID не найден
     * @throws UnauthorizedAddressAccessException если адрес не принадлежит указанному пользователю
     */
    @Override
    public void deleteAddress(UUID userId, UUID addressId) {
        log.debug("Удаление адреса с id: {} для пользователя с id: {}", addressId, userId);

        AddressEntity addressEntity = getAddressForUserOrThrow(userId, addressId);

        if (addressEntity.getIsDefault()) {
            addressRepository.findFirstByUser_IdAndIdNot(userId, addressId)
                    .ifPresent(newDefault -> {
                        newDefault.setIsDefault(true);
                        addressRepository.save(newDefault);
                    });
        }

        addressRepository.delete(addressEntity);
        log.debug("Удален адрес с id: {}", addressId);
    }

    /**
     * {@inheritDoc}
     *
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     * @throws AddressNotFoundException если адрес с указанным ID не найден
     * @throws UnauthorizedAddressAccessException если адрес не принадлежит указанному пользователю
     */
    @Override
    public void setAsDefaultAddress(UUID userId, UUID addressId) {
        log.debug("Установка адреса с id: {} дефолтным для пользователя с id: {}", addressId, userId);

        AddressEntity addressEntity = getAddressForUserOrThrow(userId, addressId);

        addressRepository.findByUser_IdAndIsDefaultTrue(userId)
                .ifPresent(existingDefault -> {
                    existingDefault.setIsDefault(false);
                    addressRepository.save(existingDefault);
                });

        addressEntity.setIsDefault(true);
        addressRepository.save(addressEntity);

        log.debug("Адрес с id: {} установлен дефолтным", addressId);
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

    /**
     * Вспомогательный метод для получения адреса, принадлежащего указанному пользователю, или выброса исключения.
     * Проверяет принадлежность адреса пользователю.
     *
     * @param userId Уникальный идентификатор пользователя
     * @param addressId Уникальный идентификатор адреса
     * @return Сущность адреса
     * @throws AddressNotFoundException если адрес с указанным ID не найден
     * @throws UnauthorizedAddressAccessException если адрес не принадлежит указанному пользователю
     */
    private AddressEntity getAddressForUserOrThrow(UUID userId, UUID addressId) {
        AddressEntity addressEntity = addressRepository.findById(addressId)
                .orElseThrow(() -> {
                    log.error("Адрес с id: {} не найден", addressId);
                    return new AddressNotFoundException(String.format("Адрес с id: %s не найден", addressId));
                });

        if (!addressEntity.getUser().getId().equals(userId)) {
            log.error("Адрес с id: {} не принадлежит пользователю с id: {}", addressId, userId);
            throw new UnauthorizedAddressAccessException(
                    String.format("Адрес с id: %s не принадлежит пользователю с id: %s", addressId, userId));
        }

        return addressEntity;
    }
}
