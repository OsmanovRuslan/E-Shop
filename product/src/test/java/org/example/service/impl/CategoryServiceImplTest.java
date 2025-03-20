package org.example.service.impl;

import org.example.data.dto.category.CategoryCreateRequest;
import org.example.data.dto.category.CategoryResponse;
import org.example.data.dto.category.CategoryUpdateRequest;
import org.example.data.entity.CategoryEntity;
import org.example.exception.category.CategoryNotFoundException;
import org.example.data.mapper.CategoryMapper;
import org.example.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private UUID categoryId;
    private CategoryEntity categoryEntity;
    private CategoryResponse categoryResponse;
    private CategoryCreateRequest createRequest;
    private CategoryUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();

        categoryEntity = new CategoryEntity();
        categoryEntity.setId(categoryId);
        categoryEntity.setName("Электроника");
        categoryEntity.setDescription("Электрические товары");
        categoryEntity.setCreatedAt(LocalDateTime.now());
        categoryEntity.setUpdatedAt(LocalDateTime.now());

        categoryResponse = new CategoryResponse(
                categoryId,
                "Электроника",
                "Электрические товары"
        );

        createRequest = new CategoryCreateRequest(
                "Электроника",
                "Электрические товары"
        );

        updateRequest = new CategoryUpdateRequest(
                "Обновленная электроника",
                "Обновленные электрические товары"
        );
    }

    @Test
    void createCategory_ShouldReturnCategoryResponse() {
        when(categoryMapper.toCategoryEntity(createRequest)).thenReturn(categoryEntity);
        when(categoryRepository.save(categoryEntity)).thenReturn(categoryEntity);
        when(categoryMapper.toCategoryDto(categoryEntity)).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.createCategory(createRequest);

        assertThat(result).isNotNull();
        assertEquals(categoryResponse, result);

        verify(categoryMapper).toCategoryEntity(any(CategoryCreateRequest.class));
        verify(categoryRepository).save(any(CategoryEntity.class));
        verify(categoryMapper).toCategoryDto(any(CategoryEntity.class));
    }

    @Test
    void getCategoryById_ShouldReturnCategoryResponse() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryEntity));
        when(categoryMapper.toCategoryDto(categoryEntity)).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.getCategoryById(categoryId);

        assertThat(result).isNotNull();
        assertEquals(categoryResponse, result);

        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper).toCategoryDto(any(CategoryEntity.class));
    }

    @Test
    void getCategoryById_WhenCategoryNotFound_ShouldThrowException() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.getCategoryById(categoryId));

        verify(categoryRepository).findById(categoryId);
        verifyNoInteractions(categoryMapper);
    }

    @Test
    void getAllCategories_ShouldReturnCategoryResponseList() {
        List<CategoryEntity> categoryEntityList = List.of(categoryEntity);
        when(categoryRepository.findAll()).thenReturn(categoryEntityList);
        when(categoryMapper.toCategoryDto(categoryEntity)).thenReturn(categoryResponse);

        List<CategoryResponse> results = categoryService.getAllCategories();

        assertThat(results).isNotNull().hasSize(1);
        assertEquals(categoryResponse, results.get(0));

        verify(categoryRepository).findAll();
        verify(categoryMapper).toCategoryDto(any(CategoryEntity.class));
    }

    @Test
    void updateCategory_ShouldReturnCategoryResponse() {
        CategoryResponse updatedResponse = new CategoryResponse(
                categoryId,
                "Обновленная электроника",
                "Обновленные электрические товары"
        );

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryEntity));
        when(categoryRepository.save(categoryEntity)).thenReturn(categoryEntity);
        when(categoryMapper.toCategoryDto(categoryEntity)).thenReturn(updatedResponse);

        CategoryResponse result = categoryService.updateCategory(categoryId, updateRequest);

        assertThat(result).isNotNull();
        assertEquals(updatedResponse, result);

        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper).updateCategoryFromDto(any(CategoryUpdateRequest.class), any(CategoryEntity.class));
        verify(categoryRepository).save(any(CategoryEntity.class));
        verify(categoryMapper).toCategoryDto(any(CategoryEntity.class));
    }

    @Test
    void updateCategory_WhenCategoryNotFound_ShouldThrowException() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.updateCategory(categoryId, updateRequest));

        verify(categoryRepository).findById(categoryId);
        verifyNoInteractions(categoryMapper);
    }

    @Test
    void deleteCategory_ShouldBeSuccess() {
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        doNothing().when(categoryRepository).deleteById(categoryId);

        categoryService.deleteCategory(categoryId);

        verify(categoryRepository).existsById(categoryId);
        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    void deleteCategory_WhenCategoryNotFound_ShouldThrowException() {
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        assertThrows(CategoryNotFoundException.class, () -> categoryService.deleteCategory(categoryId));

        verify(categoryRepository).existsById(categoryId);
        verify(categoryRepository, never()).deleteById(any(UUID.class));
    }
}