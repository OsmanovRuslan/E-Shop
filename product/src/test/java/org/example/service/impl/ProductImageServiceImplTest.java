package org.example.service.impl;

import org.example.data.dto.image.ProductImageCreateRequest;
import org.example.data.dto.image.ProductImageResponse;
import org.example.data.entity.ProductEntity;
import org.example.data.entity.ProductImageEntity;
import org.example.exception.image.ProductImageNotFoundException;
import org.example.exception.product.ProductNotFoundException;
import org.example.data.mapper.ProductImageMapper;
import org.example.repository.ProductImageRepository;
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
class ProductImageServiceImplTest {

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageMapper productImageMapper;

    @InjectMocks
    private ProductImageServiceImpl productImageService;

    private UUID productId;
    private UUID imageId;
    private UUID secondImageId;
    private ProductEntity productEntity;
    private ProductImageEntity imageEntity;
    private ProductImageEntity secondImageEntity;
    private ProductImageResponse imageResponse;
    private ProductImageCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        imageId = UUID.randomUUID();
        secondImageId = UUID.randomUUID();
        
        productEntity = new ProductEntity();
        productEntity.setId(productId);
        productEntity.setName("Iphone 16");
        productEntity.setDescription("Обновленный телефон компании Apple");
        productEntity.setPrice(999.99);
        productEntity.setCreatedBy(UUID.randomUUID());
        productEntity.setIsActive(true);
        productEntity.setCreatedAt(LocalDateTime.now());
        productEntity.setUpdatedAt(LocalDateTime.now());
        productEntity.setCategories(new HashSet<>());
        productEntity.setImages(new HashSet<>());
        
        imageEntity = new ProductImageEntity();
        imageEntity.setId(imageId);
        imageEntity.setProduct(productEntity);
        imageEntity.setImageUrl("http://example.com/image.jpg");
        imageEntity.setIsPrimary(true);
        imageEntity.setCreatedAt(LocalDateTime.now());
        imageEntity.setUpdatedAt(LocalDateTime.now());
        
        secondImageEntity = new ProductImageEntity();
        secondImageEntity.setId(secondImageId);
        secondImageEntity.setProduct(productEntity);
        secondImageEntity.setImageUrl("http://example.com/image2.jpg");
        secondImageEntity.setIsPrimary(false);
        secondImageEntity.setCreatedAt(LocalDateTime.now());
        secondImageEntity.setUpdatedAt(LocalDateTime.now());
        
        productEntity.getImages().add(imageEntity);
        productEntity.getImages().add(secondImageEntity);
        
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
    void getImageById_ShouldReturnProductImageResponse() {
        when(productImageRepository.findById(imageId)).thenReturn(Optional.of(imageEntity));
        when(productImageMapper.toProductImageDto(imageEntity)).thenReturn(imageResponse);

        ProductImageResponse result = productImageService.getImageById(imageId);

        assertThat(result).isNotNull();
        assertEquals(imageResponse, result);
        assertTrue(result.isPrimary());

        verify(productImageRepository).findById(imageId);
        verify(productImageMapper).toProductImageDto(any(ProductImageEntity.class));
    }

    @Test
    void getImageById_WhenImageNotFound_ShouldThrowException() {
        when(productImageRepository.findById(imageId)).thenReturn(Optional.empty());

        assertThrows(ProductImageNotFoundException.class, () -> productImageService.getImageById(imageId));

        verify(productImageRepository).findById(imageId);
        verifyNoInteractions(productImageMapper);
    }

    @Test
    void addImageToProduct_WhenProductHasNoImages_ShouldReturnProductImageResponse() {
        ProductEntity emptyProductEntity = new ProductEntity();
        emptyProductEntity.setId(productId);
        emptyProductEntity.setImages(new HashSet<>());

        when(productRepository.findById(productId)).thenReturn(Optional.of(emptyProductEntity));
        when(productImageMapper.toProductImageEntity(createRequest)).thenReturn(imageEntity);
        when(productRepository.save(emptyProductEntity)).thenReturn(emptyProductEntity);
        when(productImageMapper.toProductImageDto(imageEntity)).thenReturn(imageResponse);

        ProductImageResponse result = productImageService.addImageToProduct(productId, createRequest);

        assertThat(result).isNotNull();
        assertEquals(imageResponse, result);

        verify(productRepository).findById(productId);
        verify(productImageMapper).toProductImageEntity(any(ProductImageCreateRequest.class));
        verify(productRepository).save(emptyProductEntity);
        verify(productImageMapper).toProductImageDto(any(ProductImageEntity.class));
    }

    @Test
    void addImageToProduct_WithRequestedPrimary_ShouldReturnProductImageResponse() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(productEntity));
        when(productImageMapper.toProductImageEntity(createRequest)).thenReturn(imageEntity);
        when(productRepository.save(productEntity)).thenReturn(productEntity);
        when(productImageMapper.toProductImageDto(imageEntity)).thenReturn(imageResponse);

        ProductImageResponse result = productImageService.addImageToProduct(productId, createRequest);

        assertThat(result).isNotNull();
        assertEquals(imageResponse, result);

        verify(productRepository).findById(productId);
        verify(productImageMapper).toProductImageEntity(any(ProductImageCreateRequest.class));
        verify(productRepository).save(any(ProductEntity.class));
        verify(productImageMapper).toProductImageDto(any(ProductImageEntity.class));
    }

    @Test
    void addImageToProduct_WhenProductNotFound_ShouldThrowException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productImageService.addImageToProduct(productId, createRequest));

        verify(productRepository).findById(productId);
        verifyNoInteractions(productImageMapper);
    }

    @Test
    void getProductImages_ShouldReturnProductImagesList() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(productEntity));
        when(productImageMapper.toProductImageDto(imageEntity)).thenReturn(imageResponse);
        when(productImageMapper.toProductImageDto(secondImageEntity)).thenReturn(
                new ProductImageResponse(secondImageId, "http://example.com/image2.jpg", false)
        );

        List<ProductImageResponse> results = productImageService.getProductImages(productId);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);

        verify(productRepository).findById(productId);
        verify(productImageMapper, times(2)).toProductImageDto(any(ProductImageEntity.class));
    }

    @Test
    void getProductImages_WhenProductNotFound_ShouldThrowException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productImageService.getProductImages(productId));

        verify(productRepository).findById(productId);
        verifyNoInteractions(productImageMapper);
    }

    @Test
    void removeImageFromProduct_ShouldBeSuccess() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(productEntity));
        doNothing().when(productImageRepository).deleteById(imageId);
        when(productRepository.save(productEntity)).thenReturn(productEntity);

        productImageService.removeImageFromProduct(productId, imageId);

        verify(productRepository).findById(productId);
        verify(productImageRepository).deleteById(imageId);
        verify(productRepository).save(any(ProductEntity.class));
    }

    @Test
    void removeImageFromProduct_SetNewPrimaryImage_ShouldBeSuccess() {
        ProductEntity testProduct = new ProductEntity();
        testProduct.setId(productId);
        Set<ProductImageEntity> images = new HashSet<>();

        ProductImageEntity primaryImage = new ProductImageEntity();
        primaryImage.setId(imageId);
        primaryImage.setProduct(testProduct);
        primaryImage.setIsPrimary(true);

        ProductImageEntity nonPrimaryImage = new ProductImageEntity();
        nonPrimaryImage.setId(secondImageId);
        nonPrimaryImage.setProduct(testProduct);
        nonPrimaryImage.setIsPrimary(false);

        images.add(primaryImage);
        images.add(nonPrimaryImage);
        testProduct.setImages(images);

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        doNothing().when(productImageRepository).deleteById(imageId);
        when(productRepository.save(testProduct)).thenReturn(testProduct);

        productImageService.removeImageFromProduct(productId, imageId);

        verify(productRepository).findById(productId);
        verify(productImageRepository).deleteById(imageId);
        verify(productRepository).save(testProduct);
        assertTrue(nonPrimaryImage.getIsPrimary());
    }

    @Test
    void removeImageFromProduct_WhenProductNotFound_ShouldThrowException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productImageService.removeImageFromProduct(productId, imageId));

        verify(productRepository).findById(productId);
        verifyNoInteractions(productImageRepository);
    }

    @Test
    void removeImageFromProduct_WhenImageNotFound_ShouldThrowException() {
        UUID nonExistentImageId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.of(productEntity));

        assertThrows(ProductImageNotFoundException.class, () -> productImageService.removeImageFromProduct(productId, nonExistentImageId));

        verify(productRepository).findById(productId);
        verifyNoInteractions(productImageRepository);
    }

    @Test
    void setPrimaryImage_ShouldReturnProductImageResponse() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(productEntity));
        when(productRepository.save(productEntity)).thenReturn(productEntity);
        when(productImageMapper.toProductImageDto(secondImageEntity)).thenReturn(
                new ProductImageResponse(secondImageId, "http://example.com/image2.jpg", true)
        );

        ProductImageResponse result = productImageService.setPrimaryImage(productId, secondImageId);

        assertThat(result).isNotNull();
        assertEquals(secondImageId, result.id());
        assertFalse(imageEntity.getIsPrimary());
        assertTrue(secondImageEntity.getIsPrimary());

        verify(productRepository).findById(productId);
        verify(productRepository).save(productEntity);
        verify(productImageMapper).toProductImageDto(secondImageEntity);
    }

    @Test
    void setPrimaryImage_WhenProductNotFound_ShouldThrowException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productImageService.setPrimaryImage(productId, imageId));

        verify(productRepository).findById(productId);
        verifyNoInteractions(productImageMapper);
    }

    @Test
    void setPrimaryImage_WhenImageNotFound_ShouldThrowException() {
        UUID nonExistentImageId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.of(productEntity));

        assertThrows(ProductImageNotFoundException.class, () -> productImageService.setPrimaryImage(productId, nonExistentImageId));

        verify(productRepository).findById(productId);
        verifyNoInteractions(productImageMapper);
    }
}