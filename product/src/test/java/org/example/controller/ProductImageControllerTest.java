package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.data.dto.image.ProductImageCreateRequest;
import org.example.data.dto.image.ProductImageResponse;
import org.example.exception.image.ProductImageNotFoundException;
import org.example.exception.product.ProductNotFoundException;
import org.example.service.ProductImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductImageController.class)
class ProductImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductImageService productImageService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID productId;
    private UUID imageId;
    private ProductImageResponse imageResponse;
    private ProductImageCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        imageId = UUID.randomUUID();

        imageResponse = new ProductImageResponse(
                imageId,
                "http://example.com/image.jpg",
                true
        );

        createRequest = new ProductImageCreateRequest(
                "http://example.com/image.jpg",
                true
        );
    }

    @Test
    void addImageToProduct_ShouldReturnCreated() throws Exception {
        when(productImageService.addImageToProduct(eq(productId), any(ProductImageCreateRequest.class))).thenReturn(imageResponse);

        mockMvc.perform(post("/api/v1/products/{productId}/images", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(imageId.toString()))
                .andExpect(jsonPath("$.imageUrl").value("http://example.com/image.jpg"))
                .andExpect(jsonPath("$.isPrimary").value(true));

        verify(productImageService).addImageToProduct(eq(productId), any(ProductImageCreateRequest.class));
    }

    @Test
    void addImageToProduct_WhenProductNotFound_ShouldReturnNotFound() throws Exception {
        when(productImageService.addImageToProduct(eq(productId), any(ProductImageCreateRequest.class))).thenThrow(
                new ProductNotFoundException("Товар с id: " + productId + " не найден"));

        mockMvc.perform(post("/api/v1/products/{productId}/images", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound());

        verify(productImageService).addImageToProduct(eq(productId), any(ProductImageCreateRequest.class));
    }

    @Test
    void getProductImages_ShouldReturnOk() throws Exception {
        when(productImageService.getProductImages(productId)).thenReturn(List.of(imageResponse));

        mockMvc.perform(get("/api/v1/products/{productId}/images", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(imageId.toString()))
                .andExpect(jsonPath("$[0].imageUrl").value("http://example.com/image.jpg"))
                .andExpect(jsonPath("$[0].isPrimary").value(true));

        verify(productImageService).getProductImages(productId);
    }

    @Test
    void getProductImages_WhenProductNotFound_ShouldReturnNotFound() throws Exception {
        when(productImageService.getProductImages(productId)).thenThrow(
                new ProductNotFoundException("Товар с id: " + productId + " не найден"));

        mockMvc.perform(get("/api/v1/products/{productId}/images", productId))
                .andExpect(status().isNotFound());

        verify(productImageService).getProductImages(productId);
    }

    @Test
    void getImageById_ShouldReturnOk() throws Exception {
        when(productImageService.getImageById(imageId)).thenReturn(imageResponse);

        mockMvc.perform(get("/api/v1/products/{productId}/images/{imageId}", productId, imageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(imageId.toString()))
                .andExpect(jsonPath("$.imageUrl").value("http://example.com/image.jpg"))
                .andExpect(jsonPath("$.isPrimary").value(true));

        verify(productImageService).getImageById(imageId);
    }

    @Test
    void getImageById_WhenImageNotFound_ShouldReturnNotFound() throws Exception {
        when(productImageService.getImageById(imageId)).thenThrow(
                new ProductImageNotFoundException("Изображение с id: " + imageId + " не найдено"));

        mockMvc.perform(get("/api/v1/products/{productId}/images/{imageId}", productId, imageId))
                .andExpect(status().isNotFound());

        verify(productImageService).getImageById(imageId);
    }

    @Test
    void setPrimaryImage_ShouldReturnOk() throws Exception {
        when(productImageService.setPrimaryImage(productId, imageId)).thenReturn(imageResponse);

        mockMvc.perform(put("/api/v1/products/{productId}/images/{imageId}/primary", productId, imageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(imageId.toString()))
                .andExpect(jsonPath("$.imageUrl").value("http://example.com/image.jpg"))
                .andExpect(jsonPath("$.isPrimary").value(true));

        verify(productImageService).setPrimaryImage(productId, imageId);
    }

    @Test
    void setPrimaryImage_WhenImageNotFound_ShouldReturnNotFound() throws Exception {
        when(productImageService.setPrimaryImage(productId, imageId)).thenThrow(
                new ProductImageNotFoundException("Изображение с id: " + imageId + " не найдено у товара с id: " + productId));

        mockMvc.perform(put("/api/v1/products/{productId}/images/{imageId}/primary", productId, imageId))
                .andExpect(status().isNotFound());

        verify(productImageService).setPrimaryImage(productId, imageId);
    }

    @Test
    void removeImageFromProduct_ShouldReturnNoContent() throws Exception {
        doNothing().when(productImageService).removeImageFromProduct(productId, imageId);

        mockMvc.perform(delete("/api/v1/products/{productId}/images/{imageId}", productId, imageId))
                .andExpect(status().isNoContent());

        verify(productImageService).removeImageFromProduct(productId, imageId);
    }

    @Test
    void removeImageFromProduct_WhenImageNotFound_ShouldReturnNotFound() throws Exception {
        doThrow(new ProductImageNotFoundException("Изображение с id: " + imageId + " не найдено у товара с id: " + productId))
                .when(productImageService).removeImageFromProduct(productId, imageId);

        mockMvc.perform(delete("/api/v1/products/{productId}/images/{imageId}", productId, imageId))
                .andExpect(status().isNotFound());

        verify(productImageService).removeImageFromProduct(productId, imageId);
    }
}