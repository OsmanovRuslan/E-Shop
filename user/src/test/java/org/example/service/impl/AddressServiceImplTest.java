package org.example.service.impl;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    private UUID userId;
    private UUID addressId;
    private UUID anotherAddressId;
    private UserEntity userEntity;
    private AddressEntity addressEntity;
    private AddressEntity anotherAddressEntity;
    private AddressEntity defaultAddressEntity;
    private AddressResponse addressResponse;
    private AddressResponse updatedResponse;
    private AddressCreateRequest createRequest;
    private AddressUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        addressId = UUID.randomUUID();
        anotherAddressId = UUID.randomUUID();

        userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setFirstName("Василий");
        userEntity.setLastName("Лопатин");
        userEntity.setEmail("vasya.lopa@example.com");
        userEntity.setPhone("+12345678901");
        userEntity.setCreatedAt(LocalDateTime.now());
        userEntity.setUpdatedAt(LocalDateTime.now());
        userEntity.setAddresses(new HashSet<>());

        addressEntity = new AddressEntity();
        addressEntity.setId(addressId);
        addressEntity.setUser(userEntity);
        addressEntity.setStreet("ул.Пушкина д.37");
        addressEntity.setCity("Москва");
        addressEntity.setCountry("Россия");
        addressEntity.setPostalCode("10001");
        addressEntity.setIsDefault(false);
        addressEntity.setCreatedAt(LocalDateTime.now());
        addressEntity.setUpdatedAt(LocalDateTime.now());

        defaultAddressEntity = new AddressEntity();
        defaultAddressEntity.setId(anotherAddressId);
        defaultAddressEntity.setUser(userEntity);
        defaultAddressEntity.setStreet("ул.Гоголя д.45");
        defaultAddressEntity.setCity("Москва");
        defaultAddressEntity.setCountry("Россия");
        defaultAddressEntity.setPostalCode("10002");
        defaultAddressEntity.setIsDefault(true);
        defaultAddressEntity.setCreatedAt(LocalDateTime.now());
        defaultAddressEntity.setUpdatedAt(LocalDateTime.now());

        anotherAddressEntity = new AddressEntity();
        anotherAddressEntity.setId(UUID.randomUUID());
        anotherAddressEntity.setUser(userEntity);
        anotherAddressEntity.setStreet("ул.Ломоносова строение 7");
        anotherAddressEntity.setCity("Москва");
        anotherAddressEntity.setCountry("Россия");
        anotherAddressEntity.setPostalCode("10003");
        anotherAddressEntity.setIsDefault(false);
        anotherAddressEntity.setCreatedAt(LocalDateTime.now());
        anotherAddressEntity.setUpdatedAt(LocalDateTime.now());

        addressResponse = new AddressResponse(
                addressId,
                "ул.Пушкина д.37",
                "Москва",
                "Россия",
                "10001",
                false
        );

        updatedResponse = new AddressResponse(
                addressId,
                "ул.Чехова д.15",
                "Москва",
                "Россия",
                "10001",
                true
        );

        createRequest = new AddressCreateRequest(
                "ул.Пушкина д.37",
                "Москва",
                "Россия",
                "10001",
                false
        );

        updateRequest = new AddressUpdateRequest(
                "ул.Чехова д.15",
                "Москва",
                "Россия",
                "10001",
                true
        );
    }

    @Test
    void addAddress_WhenDefaultRequested_ShouldSetOtherAddressesNonDefault() {
        createRequest = new AddressCreateRequest(
                "ул.Пушкина д.37",
                "Москва",
                "Россия",
                "10001",
                true
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(addressMapper.toAddressEntity(createRequest)).thenReturn(addressEntity);
        when(addressRepository.findByUser_IdAndIsDefaultTrue(userId))
                .thenReturn(Optional.of(defaultAddressEntity));
        when(addressRepository.save(defaultAddressEntity)).thenReturn(null);
        when(addressRepository.save(addressEntity)).thenReturn(addressEntity);
        when(addressMapper.toAddressResponse(addressEntity)).thenReturn(addressResponse);

        addressService.addAddress(userId, createRequest);

        verify(userRepository).findById(userId);
        verify(addressMapper).toAddressEntity(any(AddressCreateRequest.class));
        verify(addressRepository).findByUser_IdAndIsDefaultTrue(userId);
        verify(addressRepository, times(2)).save(any(AddressEntity.class));
        verify(addressMapper).toAddressResponse(any(AddressEntity.class));
    }

    @Test
    void addAddress_WhenFirstAddress_ShouldSetAsDefault() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(addressMapper.toAddressEntity(createRequest)).thenReturn(addressEntity);
        when(addressRepository.countByUser_Id(userId)).thenReturn(0L);
        when(addressRepository.save(addressEntity)).thenReturn(addressEntity);
        when(addressMapper.toAddressResponse(addressEntity)).thenReturn(addressResponse);

        AddressResponse result = addressService.addAddress(userId, createRequest);

        assertThat(result).isNotNull();
        verify(userRepository).findById(userId);
        verify(addressMapper).toAddressEntity(any(AddressCreateRequest.class));
        verify(addressRepository).countByUser_Id(userId);
        verify(addressRepository).save(any(AddressEntity.class));
        verify(addressMapper).toAddressResponse(any(AddressEntity.class));
    }

    @Test
    void addAddress_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> addressService.addAddress(userId, createRequest));

        verify(userRepository).findById(userId);
        verifyNoInteractions(addressMapper);
        verifyNoInteractions(addressRepository);
    }

    @Test
    void getAddressById_WhenAddressExists_ShouldReturnAddress() {
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(addressEntity));
        when(addressMapper.toAddressResponse(addressEntity)).thenReturn(addressResponse);

        AddressResponse result = addressService.getAddressById(userId, addressId);

        assertThat(result).isNotNull();
        assertEquals(addressId, result.id());
        verify(addressRepository).findById(addressId);
        verify(addressMapper).toAddressResponse(any(AddressEntity.class));
    }

    @Test
    void getAddressById_WhenAddressNotFound_ShouldThrowException() {
        when(addressRepository.findById(addressId)).thenReturn(Optional.empty());

        assertThrows(AddressNotFoundException.class, () -> addressService.getAddressById(userId, addressId));

        verify(addressRepository).findById(addressId);
        verifyNoInteractions(addressMapper);
    }

    @Test
    void getAddressById_WhenAddressNotBelongToUser_ShouldThrowException() {
        UUID anotherUserId = UUID.randomUUID();
        UserEntity anotherUser = new UserEntity();
        anotherUser.setId(anotherUserId);

        AddressEntity addressWithDifferentUser = new AddressEntity();
        addressWithDifferentUser.setId(addressId);
        addressWithDifferentUser.setUser(anotherUser);

        when(addressRepository.findById(addressId)).thenReturn(Optional.of(addressWithDifferentUser));

        assertThrows(UnauthorizedAddressAccessException.class, () -> addressService.getAddressById(userId, addressId));

        verify(addressRepository).findById(addressId);
        verifyNoInteractions(addressMapper);
    }

    @Test
    void getUserAddresses_WhenUserExists_ShouldReturnAddresses() {
        Set<AddressEntity> addresses = Set.of(addressEntity, defaultAddressEntity);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(addressRepository.findByUser_Id(userId)).thenReturn(addresses);
        when(addressMapper.toAddressResponse(addressEntity)).thenReturn(addressResponse);
        when(addressMapper.toAddressResponse(defaultAddressEntity)).thenReturn(
                new AddressResponse(
                        anotherAddressId,
                        "ул.Гоголя д.45",
                        "Москва",
                        "Россия",
                        "10002",
                        true
                )
        );

        Set<AddressResponse> result = addressService.getUserAddresses(userId);

        assertThat(result).isNotNull();
        assertEquals(2, result.size());
        verify(userRepository).existsById(userId);
        verify(addressRepository).findByUser_Id(userId);
        verify(addressMapper, times(2)).toAddressResponse(any(AddressEntity.class));
    }

    @Test
    void getUserAddresses_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> addressService.getUserAddresses(userId));

        verify(userRepository).existsById(userId);
        verifyNoInteractions(addressMapper);
        verifyNoInteractions(addressRepository);
    }

    @Test
    void updateAddress_WhenAddressExists_ShouldReturnUpdatedAddress() {
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(addressEntity));
        when(addressRepository.save(defaultAddressEntity)).thenReturn(null);
        when(addressRepository.save(addressEntity)).thenReturn(addressEntity);
        when(addressMapper.toAddressResponse(addressEntity)).thenReturn(updatedResponse);
        when(addressRepository.findByUser_IdAndIsDefaultTrue(userId))
                .thenReturn(Optional.of(defaultAddressEntity));

        AddressResponse result = addressService.updateAddress(userId, addressId, updateRequest);

        assertThat(result).isNotNull();
        assertEquals(updatedResponse, result);
        assertEquals(true, result.isDefault());

        verify(addressRepository).findById(addressId);
        verify(addressMapper).updateAddressFromDto(updateRequest, addressEntity);
        verify(addressRepository).findByUser_IdAndIsDefaultTrue(userId);
        verify(addressRepository, times(2)).save(any(AddressEntity.class));
        verify(addressMapper).toAddressResponse(addressEntity);
    }

    @Test
    void updateAddress_WhenAddressNotFound_ShouldThrowException() {
        when(addressRepository.findById(addressId)).thenReturn(Optional.empty());

        assertThrows(AddressNotFoundException.class, () -> addressService.updateAddress(userId, addressId, updateRequest));

        verify(addressRepository).findById(addressId);
        verifyNoInteractions(addressMapper);
    }

    @Test
    void deleteAddress_WhenDefaultAddress_ShouldSetNewDefault() {
        addressEntity.setIsDefault(true);

        when(addressRepository.findById(addressId)).thenReturn(Optional.of(addressEntity));
        when(addressRepository.findFirstByUser_IdAndIdNot(userId, addressId))
                .thenReturn(Optional.of(anotherAddressEntity));

        addressService.deleteAddress(userId, addressId);

        verify(addressRepository).findById(addressId);
        verify(addressRepository).findFirstByUser_IdAndIdNot(userId, addressId);
        verify(addressRepository).save(any(AddressEntity.class));
        verify(addressRepository).delete(any(AddressEntity.class));
        assertTrue(anotherAddressEntity.getIsDefault());
    }

    @Test
    void deleteAddress_WhenNonDefaultAddress_ShouldDeleteWithoutSettingNewDefault() {
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(addressEntity));

        addressService.deleteAddress(userId, addressId);

        verify(addressRepository).findById(addressId);
        verify(addressRepository, never()).findFirstByUser_IdAndIdNot(any(UUID.class), any(UUID.class));
        verify(addressRepository).delete(any(AddressEntity.class));
    }

    @Test
    void setAsDefaultAddress_ShouldUpdateDefaultAddress() {
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(addressEntity));
        when(addressRepository.findByUser_IdAndIsDefaultTrue(userId))
                .thenReturn(Optional.of(defaultAddressEntity));

        addressService.setAsDefaultAddress(userId, addressId);

        verify(addressRepository).findById(addressId);
        verify(addressRepository).findByUser_IdAndIsDefaultTrue(userId);
        verify(addressRepository).save(defaultAddressEntity);
        verify(addressRepository).save(addressEntity);

        assertFalse(defaultAddressEntity.getIsDefault());
        assertTrue(addressEntity.getIsDefault());
    }


    @Test
    void setAsDefaultAddress_WhenAddressNotFound_ShouldThrowException() {
        when(addressRepository.findById(addressId)).thenReturn(Optional.empty());

        assertThrows(AddressNotFoundException.class, () -> addressService.setAsDefaultAddress(userId, addressId));

        verify(addressRepository).findById(addressId);
        verifyNoMoreInteractions(addressRepository);
    }

}