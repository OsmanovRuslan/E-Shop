package org.example.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.data.dto.image.ProductImageCreateRequest;
import org.example.data.dto.product.ProductCreateRequest;
import org.example.data.dto.product.ProductResponse;
import org.example.data.dto.product.ProductSummaryResponse;
import org.example.data.dto.product.ProductUpdateRequest;
import org.example.data.entity.CategoryEntity;
import org.example.data.entity.ProductEntity;
import org.example.data.entity.ProductImageEntity;
import org.example.exception.product.ProductNotFoundException;
import org.example.data.mapper.ProductImageMapper;
import org.example.data.mapper.ProductMapper;
import org.example.repository.CategoryRepository;
import org.example.repository.ProductRepository;
import org.example.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;

    @Override
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.debug("Создание товара с названием: {}", request.name());

        ProductEntity productEntity = productMapper.toProductEntity(request);

        if (request.images() != null && !request.images().isEmpty()) {
            for (ProductImageCreateRequest imageRequest : request.images()) {
                ProductImageEntity imageEntity = productImageMapper.toProductImageEntity(imageRequest);
                imageEntity.setProduct(productEntity);
                productEntity.getImages().add(imageEntity);
            }
        }

        if (request.categoryIds() != null && !request.categoryIds().isEmpty()) {
            List<CategoryEntity> categories = categoryRepository.findAllById(request.categoryIds());
            productEntity.getCategories().addAll(categories);
        }

        productEntity = productRepository.save(productEntity);
        log.debug("Создан товар с id: {}", productEntity.getId());

        return productMapper.toProductResponse(productEntity);
    }

    @Override
    public ProductResponse getProductById(UUID productId) {
        log.debug("Получение товара с id: {}", productId);

        ProductEntity productEntity = getProductOrThrow(productId);
        log.debug("Получен товар с id: {}", productId);

        return productMapper.toProductResponse(productEntity);
    }

    @Override
    public List<ProductResponse> getProductsByIds(List<UUID> productIds) {
        log.debug("Получение списка товаров по их id, количество: {}", productIds.size());

        List<ProductEntity> products = productRepository.findAllById(productIds);

        if (products.size() < productIds.size()) {
            log.warn("Не все товары найдены. Запрошено: {}, найдено: {}",
                    productIds.size(), products.size());
        }

        log.debug("Получено {} товаров", products.size());
        return products.stream()
                .map(productMapper::toProductResponse)
                .toList();
    }

    @Override
    public List<ProductSummaryResponse> getAllProducts() {
        log.debug("Получение списка всех товаров");

        List<ProductEntity> products = productRepository.findAll();
        log.debug("Получено {} товаров", products.size());

        return products.stream()
                .map(productMapper::toProductSummaryResponse)
                .toList();
    }

    @Override
    public List<ProductSummaryResponse> getProductsByCategory(UUID categoryId) {
        log.debug("Получение товаров категории с id: {}", categoryId);

        List<ProductEntity> products = productRepository.findByCategoryId(categoryId);

        log.debug("Получено {} товаров категории с id: {}", products.size(), categoryId);

        return products.stream()
                .map(productMapper::toProductSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse updateProduct(UUID productId, ProductUpdateRequest request) {
        log.debug("Обновление товара с id: {}", productId);

        ProductEntity productEntity = getProductOrThrow(productId);

        productMapper.updateProductFromDto(request, productEntity);

        if (request.images() != null) {
            productEntity.getImages().clear();
            for (ProductImageCreateRequest imageRequest : request.images()) {
                ProductImageEntity imageEntity = productImageMapper.toProductImageEntity(imageRequest);
                imageEntity.setProduct(productEntity);
                productEntity.getImages().add(imageEntity);
            }
        }

        if (request.categoryIds() != null) {
            productEntity.getCategories().clear();
            if (!request.categoryIds().isEmpty()) {
                List<CategoryEntity> categories = categoryRepository.findAllById(request.categoryIds());
                productEntity.getCategories().addAll(categories);
            }
        }

        productEntity = productRepository.save(productEntity);
        log.debug("Обновлен товар с id: {}", productId);

        return productMapper.toProductResponse(productEntity);
    }

    @Override
    public void deleteProduct(UUID productId) {
        log.debug("Удаление товара с id: {}", productId);

        if (!productRepository.existsById(productId)) {
            log.error("Товар с id: {} не найден", productId);
            throw new ProductNotFoundException(String.format("Товар с id: %s не найден", productId));
        }

        productRepository.deleteById(productId);
        log.debug("Удален товар с id: {}", productId);
    }

    @Override
    public List<ProductSummaryResponse> searchProducts(String query) {
        log.debug("Поиск товаров по запросу: {}", query);

        String searchPattern = "%" + query.toLowerCase() + "%";
        List<ProductEntity> products = productRepository.searchByNameOrDescription(searchPattern);

        log.debug("Найдено {} товаров по запросу: {}", products.size(), query);

        return products.stream()
                .map(productMapper::toProductSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse setProductActive(UUID productId, boolean active) {
        log.debug("Установка активности товара с id: {} в {}", productId, active);

        ProductEntity productEntity = getProductOrThrow(productId);
        productEntity.setIsActive(active);
        productEntity = productRepository.save(productEntity);

        log.debug("Установлена активность товара с id: {} в {}", productId, active);

        return productMapper.toProductResponse(productEntity);
    }

    private ProductEntity getProductOrThrow(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Товар с id: {} не найден", productId);
                    return new ProductNotFoundException(String.format("Товар с id: %s не найден", productId));
                });
    }
}
