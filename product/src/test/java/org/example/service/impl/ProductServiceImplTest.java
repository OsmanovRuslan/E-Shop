package org.example.service.impl;

import org.example.data.dto.image.ProductImageCreateRequest;
import org.example.data.dto.image.ProductImageResponse;
import org.example.data.dto.product.ProductCreateRequest;
import org.example.data.dto.product.ProductResponse;
import org.example.data.dto.product.ProductSummaryResponse;
import org.example.data.dto.product.ProductUpdateRequest;
import org.example.data.dto.category.CategoryResponse;
import org.example.data.entity.CategoryEntity;
import org.example.data.entity.ProductEntity;
import org.example.data.entity.ProductImageEntity;
import org.example.exception.product.ProductNotFoundException;
import org.example.data.mapper.ProductImageMapper;
import org.example.data.mapper.ProductMapper;
import org.example.repository.CategoryRepository;
import org.example.repository.ProductRepository;
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
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductImageMapper productImageMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private UUID productId;
    private UUID categoryId;
    private UUID imageId;
    private ProductEntity productEntity;
    private CategoryEntity categoryEntity;
    private ProductImageEntity productImageEntity;
    private ProductResponse productResponse;
    private ProductSummaryResponse productSummaryResponse;
    private ProductCreateRequest productCreateRequest;
    private ProductUpdateRequest productUpdateRequest;
    private ProductImageCreateRequest productImageCreateRequest;
    private ProductImageResponse productImageResponse;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        imageId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();

        categoryEntity = new CategoryEntity();
        categoryEntity.setId(categoryId);
        categoryEntity.setName("Электроника");
        categoryEntity.setDescription("Электрические товары");
        categoryEntity.setCreatedAt(LocalDateTime.now());
        categoryEntity.setUpdatedAt(LocalDateTime.now());

        productImageEntity = new ProductImageEntity();
        productImageEntity.setId(imageId);
        productImageEntity.setImageUrl("http://example.com/image.jpg");
        productImageEntity.setIsPrimary(true);
        productImageEntity.setCreatedAt(LocalDateTime.now());
        productImageEntity.setUpdatedAt(LocalDateTime.now());

        productEntity = new ProductEntity();
        productEntity.setId(productId);
        productEntity.setName("Iphone 16");
        productEntity.setDescription("Телефон компании Apple");
        productEntity.setPrice(999.99);
        productEntity.setCreatedBy(createdBy);
        productEntity.setIsActive(true);
        productEntity.setCreatedAt(LocalDateTime.now());
        productEntity.setUpdatedAt(LocalDateTime.now());
        productEntity.setCategories(new HashSet<>(Collections.singletonList(categoryEntity)));
        productEntity.setImages(new HashSet<>(Collections.singletonList(productImageEntity)));
        productImageEntity.setProduct(productEntity);

        categoryResponse = new CategoryResponse(
                categoryId,
                "Электроника",
                "Электрические товары"
        );

        productImageResponse = new ProductImageResponse(
                imageId,
                "http://example.com/image.jpg",
                true
        );

        productResponse = new ProductResponse(
                productId,
                "Iphone 16",
                "Телефон компании Apple",
                999.99,
                true,
                List.of(categoryResponse),
                List.of(productImageResponse),
                productImageResponse
        );

        productSummaryResponse = new ProductSummaryResponse(
                productId,
                "Iphone 16",
                999.99,
                true,
                productImageResponse
        );

        productImageCreateRequest = new ProductImageCreateRequest(
                "http://example.com/image.jpg",
                true
        );

        productCreateRequest = new ProductCreateRequest(
                "Iphone 16",
                "Телефон компании Apple",
                999.99,
                createdBy,
                true,
                List.of(categoryId),
                List.of(productImageCreateRequest)
        );

        productUpdateRequest = new ProductUpdateRequest(
                "Обновленный Iphone 16",
                "Обновленный телефон компании Apple",
                1099.99,
                true,
                List.of(categoryId),
                List.of(productImageCreateRequest)
        );
    }

    @Test
    void createProduct_ShouldReturnProductResponse() {
        when(productMapper.toProductEntity(productCreateRequest)).thenReturn(productEntity);
        when(productImageMapper.toProductImageEntity(any(ProductImageCreateRequest.class)))
                .thenReturn(productImageEntity);
        when(categoryRepository.findAllById(any())).thenReturn(List.of(categoryEntity));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(productEntity);
        when(productMapper.toProductResponse(productEntity)).thenReturn(productResponse);

        ProductResponse result = productService.createProduct(productCreateRequest);

        assertThat(result).isNotNull();
        assertEquals(productResponse, result);

        verify(productMapper).toProductEntity(any(ProductCreateRequest.class));
        verify(productImageMapper).toProductImageEntity(any(ProductImageCreateRequest.class));
        verify(categoryRepository).findAllById(any());
        verify(productRepository).save(any(ProductEntity.class));
        verify(productMapper).toProductResponse(any(ProductEntity.class));
    }

    @Test
    void getProductById_ShouldReturnProductResponse() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(productEntity));
        when(productMapper.toProductResponse(productEntity)).thenReturn(productResponse);

        ProductResponse result = productService.getProductById(productId);

        assertThat(result).isNotNull();
        assertEquals(productResponse, result);

        verify(productRepository).findById(productId);
        verify(productMapper).toProductResponse(any(ProductEntity.class));
    }

    @Test
    void getProductById_ThrowsException_WhenProductNotFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(productId));

        verify(productRepository).findById(productId);
        verifyNoInteractions(productMapper);
    }

    @Test
    void getProductsByIds_ShouldReturnProductResponse() {
        List<UUID> productIds = List.of(productId);
        when(productRepository.findAllById(productIds)).thenReturn(List.of(productEntity));
        when(productMapper.toProductResponse(productEntity)).thenReturn(productResponse);

        List<ProductResponse> results = productService.getProductsByIds(productIds);

        assertThat(results).isNotNull().hasSize(1);
        assertEquals(productResponse, results.get(0));

        verify(productRepository).findAllById(productIds);
        verify(productMapper).toProductResponse(any(ProductEntity.class));
    }

    @Test
    void getAllProducts_ShouldReturnProductSummaryResponseList() {
        when(productRepository.findAll()).thenReturn(List.of(productEntity));
        when(productMapper.toProductSummaryResponse(productEntity)).thenReturn(productSummaryResponse);

        List<ProductSummaryResponse> results = productService.getAllProducts();

        assertThat(results).isNotNull().hasSize(1);
        assertEquals(productSummaryResponse, results.get(0));

        verify(productRepository).findAll();
        verify(productMapper).toProductSummaryResponse(any(ProductEntity.class));
    }

    @Test
    void getProductsByCategory_ShouldReturnProductSummaryResponseList() {
        when(productRepository.findByCategoryId(categoryId)).thenReturn(List.of(productEntity));
        when(productMapper.toProductSummaryResponse(productEntity)).thenReturn(productSummaryResponse);

        List<ProductSummaryResponse> results = productService.getProductsByCategory(categoryId);

        assertThat(results).isNotNull().hasSize(1);
        assertEquals(productSummaryResponse, results.get(0));

        verify(productRepository).findByCategoryId(categoryId);
        verify(productMapper).toProductSummaryResponse(any(ProductEntity.class));
    }

    @Test
    void updateProduct_Success() {
        ProductResponse updatedProductResponse = new ProductResponse(
                productId,
                "Обновленный Iphone 16",
                "Обновленный телефон компании Apple",
                1099.99,
                true,
                List.of(categoryResponse),
                List.of(productImageResponse),
                productImageResponse
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(productEntity));
        when(productImageMapper.toProductImageEntity(productImageCreateRequest)).thenReturn(productImageEntity);
        when(categoryRepository.findAllById(any())).thenReturn(List.of(categoryEntity));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(productEntity);
        when(productMapper.toProductResponse(productEntity)).thenReturn(updatedProductResponse);

        ProductResponse result = productService.updateProduct(productId, productUpdateRequest);

        assertThat(result).isNotNull();
        assertEquals(updatedProductResponse, result);

        verify(productRepository).findById(productId);
        verify(productMapper).updateProductFromDto(any(ProductUpdateRequest.class), any(ProductEntity.class));
        verify(productImageMapper).toProductImageEntity(any(ProductImageCreateRequest.class));
        verify(categoryRepository).findAllById(any());
        verify(productRepository).save(any(ProductEntity.class));
        verify(productMapper).toProductResponse(any(ProductEntity.class));
    }

    @Test
    void updateProduct_ThrowsException_WhenProductNotFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.updateProduct(productId, productUpdateRequest));

        verify(productRepository).findById(productId);
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(categoryRepository);
        verifyNoInteractions(productMapper);
    }

    @Test
    void deleteProduct_ShouldBeSuccess() {
        when(productRepository.existsById(productId)).thenReturn(true);

        productService.deleteProduct(productId);

        verify(productRepository).existsById(productId);
        verify(productRepository).deleteById(productId);
    }

    @Test
    void searchProducts_ShouldReturnProductSummaryResponseList() {
        String query = "Iphone";
        String searchPattern = "%" + query.toLowerCase() + "%";
        when(productRepository.searchByNameOrDescription(searchPattern)).thenReturn(List.of(productEntity));
        when(productMapper.toProductSummaryResponse(productEntity)).thenReturn(productSummaryResponse);

        List<ProductSummaryResponse> results = productService.searchProducts(query);

        assertThat(results).isNotNull().hasSize(1);
        assertEquals(productSummaryResponse, results.get(0));

        verify(productRepository).searchByNameOrDescription(searchPattern);
        verify(productMapper).toProductSummaryResponse(any(ProductEntity.class));
    }

    @Test
    void setProductActive_ShouldReturnDisabledProductResponse() {
        ProductResponse inactiveProductResponse = new ProductResponse(
                productId,
                "Iphone 16",
                "Телефон компании Apple",
                999.99,
                false,
                List.of(categoryResponse),
                List.of(productImageResponse),
                productImageResponse
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(productEntity));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(productEntity);
        when(productMapper.toProductResponse(productEntity)).thenReturn(inactiveProductResponse);

        ProductResponse result = productService.setProductActive(productId, false);

        assertThat(result).isNotNull();
        assertEquals(inactiveProductResponse, result);
        assertFalse(result.isActive());

        verify(productRepository).findById(productId);
        verify(productRepository).save(any(ProductEntity.class));
        verify(productMapper).toProductResponse(any(ProductEntity.class));
    }

    @Test
    void setProductActive_WhenProductNotFound_ShouldThrowException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.setProductActive(productId, false));

        verify(productRepository).findById(productId);
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(productMapper);
    }

}