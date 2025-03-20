package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.data.dto.category.CategoryCreateRequest;
import org.example.data.dto.category.CategoryResponse;
import org.example.data.dto.category.CategoryUpdateRequest;
import org.example.exception.category.CategoryNotFoundException;
import org.example.service.CategoryService;
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

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID categoryId;
    private CategoryResponse categoryResponse;
    private CategoryCreateRequest createRequest;
    private CategoryUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();

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
    void createCategory_ShouldReturnCreated() throws Exception {
        when(categoryService.createCategory(any(CategoryCreateRequest.class))).thenReturn(categoryResponse);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Электроника"))
                .andExpect(jsonPath("$.description").value("Электрические товары"));

        verify(categoryService).createCategory(any(CategoryCreateRequest.class));
    }

    @Test
    void getCategoryById_ShouldReturnOk() throws Exception {
        when(categoryService.getCategoryById(categoryId)).thenReturn(categoryResponse);

        mockMvc.perform(get("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Электроника"))
                .andExpect(jsonPath("$.description").value("Электрические товары"));

        verify(categoryService).getCategoryById(categoryId);
    }

    @Test
    void getCategoryById_WhenCategoryNotFound_ShouldReturnNotFound() throws Exception {
        when(categoryService.getCategoryById(categoryId)).thenThrow(
                new CategoryNotFoundException("Категория с id: " + categoryId + " не найдена"));

        mockMvc.perform(get("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isNotFound());

        verify(categoryService).getCategoryById(categoryId);
    }

    @Test
    void getAllCategories_ShouldReturnOk() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(categoryResponse));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(categoryId.toString()))
                .andExpect(jsonPath("$[0].name").value("Электроника"))
                .andExpect(jsonPath("$[0].description").value("Электрические товары"));

        verify(categoryService).getAllCategories();
    }

    @Test
    void updateCategory_ShouldReturnOk() throws Exception {
        CategoryResponse updatedResponse = new CategoryResponse(
                categoryId,
                "Обновленная электроника",
                "Обновленные электрические товары"
        );

        when(categoryService.updateCategory(eq(categoryId), any(CategoryUpdateRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Обновленная электроника"))
                .andExpect(jsonPath("$.description").value("Обновленные электрические товары"));

        verify(categoryService).updateCategory(eq(categoryId), any(CategoryUpdateRequest.class));
    }

    @Test
    void updateCategory_WhenCategoryNotFound_ShouldReturnNotFound() throws Exception {
        when(categoryService.updateCategory(eq(categoryId), any(CategoryUpdateRequest.class))).thenThrow(
                new CategoryNotFoundException("Категория с id: " + categoryId + " не найдена"));

        mockMvc.perform(put("/api/v1/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(categoryService).updateCategory(eq(categoryId), any(CategoryUpdateRequest.class));
    }

    @Test
    void deleteCategory_ShouldReturnNoContent() throws Exception {
        doNothing().when(categoryService).deleteCategory(categoryId);

        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory(categoryId);
    }

    @Test
    void deleteCategory_WhenCategoryNotFound_ShouldReturnNotFound() throws Exception {
        doThrow(new CategoryNotFoundException("Категория с id: " + categoryId + " не найдена"))
                .when(categoryService).deleteCategory(categoryId);

        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isNotFound());

        verify(categoryService).deleteCategory(categoryId);
    }
}