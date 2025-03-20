package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.data.dto.category.CategoryResponse;
import org.example.data.dto.image.ProductImageCreateRequest;
import org.example.data.dto.image.ProductImageResponse;
import org.example.data.dto.product.ProductCreateRequest;
import org.example.data.dto.product.ProductResponse;
import org.example.data.dto.product.ProductSummaryResponse;
import org.example.data.dto.product.ProductUpdateRequest;
import org.example.exception.product.ProductNotFoundException;
import org.example.service.ProductService;
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

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID productId;
    private UUID categoryId;
    private UUID imageId;
    private UUID createdBy;
    private ProductResponse productResponse;
    private ProductSummaryResponse productSummaryResponse;
    private ProductCreateRequest createRequest;
    private ProductUpdateRequest updateRequest;
    private ProductImageResponse primaryImageResponse;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        imageId = UUID.randomUUID();
        createdBy = UUID.randomUUID();

        primaryImageResponse = new ProductImageResponse(
                imageId,
                "http://example.com/image.jpg",
                true
        );

        categoryResponse = new CategoryResponse(
                categoryId,
                "Электроника",
                "Электрические товары"
        );

        productResponse = new ProductResponse(
                productId,
                "Iphone 16",
                "Телефон компании Apple",
                999.99,
                true,
                List.of(categoryResponse),
                List.of(primaryImageResponse),
                primaryImageResponse
        );

        productSummaryResponse = new ProductSummaryResponse(
                productId,
                "Iphone 16",
                999.99,
                true,
                primaryImageResponse
        );

        ProductImageCreateRequest imageCreateRequest = new ProductImageCreateRequest(
                "http://example.com/image.jpg",
                true
        );

        createRequest = new ProductCreateRequest(
                "Iphone 16",
                "Телефон компании Apple",
                999.99,
                createdBy,
                true,
                List.of(categoryId),
                List.of(imageCreateRequest)
        );

        updateRequest = new ProductUpdateRequest(
                "Обновленный Iphone 16",
                "Обновленный телефон компании Apple",
                1099.99,
                true,
                List.of(categoryId),
                List.of(imageCreateRequest)
        );
    }

    @Test
    void createProduct_ShouldReturnCreated() throws Exception {
        when(productService.createProduct(any(ProductCreateRequest.class))).thenReturn(productResponse);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Iphone 16"))
                .andExpect(jsonPath("$.description").value("Телефон компании Apple"))
                .andExpect(jsonPath("$.price").value(999.99))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.categories", hasSize(1)))
                .andExpect(jsonPath("$.images", hasSize(1)))
                .andExpect(jsonPath("$.primaryImage").exists());

        verify(productService).createProduct(any(ProductCreateRequest.class));
    }

    @Test
    void getProductById_ShouldReturnOk() throws Exception {
        when(productService.getProductById(productId)).thenReturn(productResponse);

        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Iphone 16"))
                .andExpect(jsonPath("$.description").value("Телефон компании Apple"))
                .andExpect(jsonPath("$.price").value(999.99))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.categories", hasSize(1)))
                .andExpect(jsonPath("$.images", hasSize(1)))
                .andExpect(jsonPath("$.primaryImage").exists());

        verify(productService).getProductById(productId);
    }

    @Test
    void getProductById_WhenProductNotFound_ShouldReturnNotFound() throws Exception {
        when(productService.getProductById(productId)).thenThrow(
                new ProductNotFoundException("Товар с id: " + productId + " не найден"));

        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isNotFound());

        verify(productService).getProductById(productId);
    }

    @Test
    void getProductsByIds_ShouldReturnOk() throws Exception {
        List<UUID> ids = List.of(productId);
        when(productService.getProductsByIds(ids)).thenReturn(List.of(productResponse));

        mockMvc.perform(get("/api/v1/products/batch")
                        .param("ids", productId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(productId.toString()))
                .andExpect(jsonPath("$[0].name").value("Iphone 16"));

        verify(productService).getProductsByIds(ids);
    }

    @Test
    void getAllProducts_ShouldReturnOk() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(productSummaryResponse));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(productId.toString()))
                .andExpect(jsonPath("$[0].name").value("Iphone 16"))
                .andExpect(jsonPath("$[0].price").value(999.99))
                .andExpect(jsonPath("$[0].isActive").value(true))
                .andExpect(jsonPath("$[0].primaryImage").exists());

        verify(productService).getAllProducts();
    }

    @Test
    void getProductsByCategory_ShouldReturnOk() throws Exception {
        when(productService.getProductsByCategory(categoryId)).thenReturn(List.of(productSummaryResponse));

        mockMvc.perform(get("/api/v1/products/category/{categoryId}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(productId.toString()))
                .andExpect(jsonPath("$[0].name").value("Iphone 16"))
                .andExpect(jsonPath("$[0].price").value(999.99));

        verify(productService).getProductsByCategory(categoryId);
    }

    @Test
    void updateProduct_ShouldReturnOk() throws Exception {
        when(productService.updateProduct(eq(productId), any(ProductUpdateRequest.class))).thenReturn(productResponse);

        mockMvc.perform(put("/api/v1/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Iphone 16"));

        verify(productService).updateProduct(eq(productId), any(ProductUpdateRequest.class));
    }

    @Test
    void updateProduct_WhenProductNotFound_ShouldReturnNotFound() throws Exception {
        when(productService.updateProduct(eq(productId), any(ProductUpdateRequest.class))).thenThrow(
                new ProductNotFoundException("Товар с id: " + productId + " не найден"));

        mockMvc.perform(put("/api/v1/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(productService).updateProduct(eq(productId), any(ProductUpdateRequest.class));
    }

    @Test
    void deleteProduct_ShouldReturnNoContent() throws Exception {
        doNothing().when(productService).deleteProduct(productId);

        mockMvc.perform(delete("/api/v1/products/{id}", productId))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(productId);
    }

    @Test
    void deleteProduct_WhenProductNotFound_ShouldReturnNotFound() throws Exception {
        doThrow(new ProductNotFoundException("Товар с id: " + productId + " не найден"))
                .when(productService).deleteProduct(productId);

        mockMvc.perform(delete("/api/v1/products/{id}", productId))
                .andExpect(status().isNotFound());

        verify(productService).deleteProduct(productId);
    }

    @Test
    void searchProducts_ShouldReturnOk() throws Exception {
        String query = "phone";
        when(productService.searchProducts(query)).thenReturn(List.of(productSummaryResponse));

        mockMvc.perform(get("/api/v1/products/search")
                        .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(productId.toString()))
                .andExpect(jsonPath("$[0].name").value("Iphone 16"));

        verify(productService).searchProducts(query);
    }

    @Test
    void setProductActive_ShouldReturnOk() throws Exception {
        when(productService.setProductActive(productId, true)).thenReturn(productResponse);

        mockMvc.perform(put("/api/v1/products/{id}/active", productId)
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(productService).setProductActive(productId, true);
    }

    @Test
    void setProductActive_WhenProductNotFound_ShouldReturnNotFound() throws Exception {
        when(productService.setProductActive(productId, true)).thenThrow(
                new ProductNotFoundException("Товар с id: " + productId + " не найден"));

        mockMvc.perform(put("/api/v1/products/{id}/active", productId)
                        .param("active", "true"))
                .andExpect(status().isNotFound());

        verify(productService).setProductActive(productId, true);
    }
}