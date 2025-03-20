package org.example.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.data.dto.category.CategoryCreateRequest;
import org.example.data.dto.category.CategoryResponse;
import org.example.data.dto.category.CategoryUpdateRequest;
import org.example.data.entity.CategoryEntity;
import org.example.exception.category.CategoryNotFoundException;
import org.example.data.mapper.CategoryMapper;
import org.example.repository.CategoryRepository;
import org.example.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        log.debug("Создание категории с названием: {}", request.name());

        CategoryEntity categoryEntity = categoryMapper.toCategoryEntity(request);
        categoryEntity = categoryRepository.save(categoryEntity);

        log.debug("Создана категория с id: {}", categoryEntity.getId());
        return categoryMapper.toCategoryDto(categoryEntity);
    }

    @Override
    public CategoryResponse getCategoryById(UUID categoryId) {
        log.debug("Получение категории с id: {}", categoryId);

        CategoryEntity categoryEntity = getCategoryOrThrow(categoryId);

        log.debug("Получена категория с id: {}", categoryId);
        return categoryMapper.toCategoryDto(categoryEntity);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        log.debug("Получение списка всех категорий");

        List<CategoryEntity> categories = categoryRepository.findAll();

        log.debug("Получено {} категорий", categories.size());
        return categories.stream()
                .map(categoryMapper::toCategoryDto)
                .toList();
    }

    @Override
    public CategoryResponse updateCategory(UUID categoryId, CategoryUpdateRequest request) {
        log.debug("Обновление категории с id: {}", categoryId);

        CategoryEntity categoryEntity = getCategoryOrThrow(categoryId);

        categoryMapper.updateCategoryFromDto(request, categoryEntity);

        categoryEntity = categoryRepository.save(categoryEntity);

        log.debug("Обновлена категория с id: {}", categoryId);
        return categoryMapper.toCategoryDto(categoryEntity);
    }

    @Override
    public void deleteCategory(UUID categoryId) {
        log.debug("Удаление категории с id: {}", categoryId);

        if (!categoryRepository.existsById(categoryId)) {
            log.error("Категория с id: {} не найдена", categoryId);
            throw new CategoryNotFoundException(String.format("Категория с id: %s не найдена", categoryId));
        }

        categoryRepository.deleteById(categoryId);
        log.debug("Удалена категория с id: {}", categoryId);
    }

    private CategoryEntity getCategoryOrThrow(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.error("Категория с id: {} не найдена", categoryId);
                    return new CategoryNotFoundException(String.format("Категория с id: %s не найдена", categoryId));
                });
    }

}
