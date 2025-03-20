package org.example.repository;

import org.example.data.entity.ProductImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Репозиторий для работы с сущностями изображений товаров.
 * Предоставляет методы для сохранения, получения, обновления и удаления изображений товаров.
 */
public interface ProductImageRepository extends JpaRepository<ProductImageEntity, UUID> {

}
