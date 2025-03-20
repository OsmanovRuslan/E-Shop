package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.data.dto.address.AddressCreateRequest;
import org.example.data.dto.address.AddressResponse;
import org.example.data.dto.address.AddressUpdateRequest;
import org.example.exception.address.AddressNotFoundException;
import org.example.exception.address.UnauthorizedAddressAccessException;
import org.example.exception.user.UserNotFoundException;
import org.example.service.AddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AddressController.class)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddressService addressService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private UUID addressId;
    private AddressResponse addressResponse;
    private AddressResponse updatedResponse;
    private AddressResponse defaultAddressResponse;
    private AddressCreateRequest createRequest;
    private AddressUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        addressId = UUID.randomUUID();

        addressResponse = new AddressResponse(
                addressId,
                "123 Main St",
                "New York",
                "USA",
                "10001",
                false
        );

        updatedResponse = new AddressResponse(
                addressId,
                "123 Broadway",
                "New York",
                "USA",
                "10001",
                true
        );

        defaultAddressResponse = new AddressResponse(
                UUID.randomUUID(),
                "456 Park Ave",
                "New York",
                "USA",
                "10002",
                true
        );

        createRequest = new AddressCreateRequest(
                "123 Main St",
                "New York",
                "USA",
                "10001",
                false
        );

        updateRequest = new AddressUpdateRequest(
                "123 Broadway",
                "New York",
                "USA",
                "10001",
                true
        );
    }

    @Test
    void createAddress_ShouldReturnCreated() throws Exception {
        when(addressService.addAddress(eq(userId), any(AddressCreateRequest.class))).thenReturn(addressResponse);

        mockMvc.perform(post("/api/v1/users/{userId}/addresses", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(addressId.toString()))
                .andExpect(jsonPath("$.street").value("123 Main St"))
                .andExpect(jsonPath("$.city").value("New York"))
                .andExpect(jsonPath("$.country").value("USA"))
                .andExpect(jsonPath("$.postalCode").value("10001"))
                .andExpect(jsonPath("$.isDefault").value(false));

        verify(addressService).addAddress(eq(userId), any(AddressCreateRequest.class));
    }

    @Test
    void createAddress_WhenUserNotFound_ShouldReturnNotFound() throws Exception {
        when(addressService.addAddress(eq(userId), any(AddressCreateRequest.class))).thenThrow(
                new UserNotFoundException("Пользователь с id: " + userId + " не найден"));

        mockMvc.perform(post("/api/v1/users/{userId}/addresses", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound());

        verify(addressService).addAddress(eq(userId), any(AddressCreateRequest.class));
    }

    @Test
    void getAddress_ShouldReturnOk() throws Exception {
        when(addressService.getAddressById(userId, addressId)).thenReturn(addressResponse);

        mockMvc.perform(get("/api/v1/users/{userId}/addresses/{addressId}", userId, addressId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(addressId.toString()))
                .andExpect(jsonPath("$.street").value("123 Main St"))
                .andExpect(jsonPath("$.city").value("New York"))
                .andExpect(jsonPath("$.country").value("USA"))
                .andExpect(jsonPath("$.postalCode").value("10001"))
                .andExpect(jsonPath("$.isDefault").value(false));

        verify(addressService).getAddressById(userId, addressId);
    }

    @Test
    void getAddress_WhenAddressNotFound_ShouldReturnNotFound() throws Exception {
        when(addressService.getAddressById(userId, addressId)).thenThrow(
                new AddressNotFoundException("Адрес с id: " + addressId + " не найден"));

        mockMvc.perform(get("/api/v1/users/{userId}/addresses/{addressId}", userId, addressId))
                .andExpect(status().isNotFound());

        verify(addressService).getAddressById(userId, addressId);
    }

    @Test
    void getAddress_WhenUnauthorizedAccess_ShouldReturnForbidden() throws Exception {
        when(addressService.getAddressById(userId, addressId)).thenThrow(
                new UnauthorizedAddressAccessException("Адрес с id: " + addressId + " не принадлежит пользователю с id: " + userId));

        mockMvc.perform(get("/api/v1/users/{userId}/addresses/{addressId}", userId, addressId))
                .andExpect(status().isForbidden());

        verify(addressService).getAddressById(userId, addressId);
    }

    @Test
    void getUserAddresses_ShouldReturnOk() throws Exception {
        Set<AddressResponse> addresses = Set.of(addressResponse, defaultAddressResponse);
        when(addressService.getUserAddresses(userId)).thenReturn(addresses);

        mockMvc.perform(get("/api/v1/users/{userId}/addresses", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.id=='%s')].street", addressId.toString()).value("123 Main St"))
                .andExpect(jsonPath("$[?(@.isDefault==true)].street").value("456 Park Ave"));

        verify(addressService).getUserAddresses(userId);
    }

    @Test
    void getUserAddresses_WhenUserNotFound_ShouldReturnNotFound() throws Exception {
        when(addressService.getUserAddresses(userId)).thenThrow(
                new UserNotFoundException("Пользователь с id: " + userId + " не найден"));

        mockMvc.perform(get("/api/v1/users/{userId}/addresses", userId))
                .andExpect(status().isNotFound());

        verify(addressService).getUserAddresses(userId);
    }

    @Test
    void updateAddress_ShouldReturnOk() throws Exception {
        when(addressService.updateAddress(eq(userId), eq(addressId), any(AddressUpdateRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/users/{userId}/addresses/{addressId}", userId, addressId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(addressId.toString()))
                .andExpect(jsonPath("$.street").value("123 Broadway"))
                .andExpect(jsonPath("$.city").value("New York"))
                .andExpect(jsonPath("$.country").value("USA"))
                .andExpect(jsonPath("$.postalCode").value("10001"))
                .andExpect(jsonPath("$.isDefault").value(true));

        verify(addressService).updateAddress(eq(userId), eq(addressId), any(AddressUpdateRequest.class));
    }

    @Test
    void updateAddress_WhenAddressNotFound_ShouldReturnNotFound() throws Exception {
        when(addressService.updateAddress(eq(userId), eq(addressId), any(AddressUpdateRequest.class))).thenThrow(
                new AddressNotFoundException("Адрес с id: " + addressId + " не найден"));

        mockMvc.perform(put("/api/v1/users/{userId}/addresses/{addressId}", userId, addressId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(addressService).updateAddress(eq(userId), eq(addressId), any(AddressUpdateRequest.class));
    }

    @Test
    void deleteAddress_ShouldReturnNoContent() throws Exception {
        doNothing().when(addressService).deleteAddress(userId, addressId);

        mockMvc.perform(delete("/api/v1/users/{userId}/addresses/{addressId}", userId, addressId))
                .andExpect(status().isNoContent());

        verify(addressService).deleteAddress(userId, addressId);
    }

    @Test
    void deleteAddress_WhenAddressNotFound_ShouldReturnNotFound() throws Exception {
        doThrow(new AddressNotFoundException("Адрес с id: " + addressId + " не найден"))
                .when(addressService).deleteAddress(userId, addressId);

        mockMvc.perform(delete("/api/v1/users/{userId}/addresses/{addressId}", userId, addressId))
                .andExpect(status().isNotFound());

        verify(addressService).deleteAddress(userId, addressId);
    }

    @Test
    void setAsDefaultAddress_ShouldReturnOk() throws Exception {
        doNothing().when(addressService).setAsDefaultAddress(userId, addressId);

        mockMvc.perform(put("/api/v1/users/{userId}/addresses/{addressId}/default", userId, addressId))
                .andExpect(status().isOk());

        verify(addressService).setAsDefaultAddress(userId, addressId);
    }

    @Test
    void setAsDefaultAddress_WhenAddressNotFound_ShouldReturnNotFound() throws Exception {
        doThrow(new AddressNotFoundException("Адрес с id: " + addressId + " не найден"))
                .when(addressService).setAsDefaultAddress(userId, addressId);

        mockMvc.perform(put("/api/v1/users/{userId}/addresses/{addressId}/default", userId, addressId))
                .andExpect(status().isNotFound());

        verify(addressService).setAsDefaultAddress(userId, addressId);
    }
}