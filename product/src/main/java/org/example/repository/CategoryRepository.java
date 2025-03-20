package org.example.repository;

import org.example.data.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Репозиторий для работы с сущностями категорий.
 * Предоставляет методы для сохранения, получения, обновления и удаления категорий.
 */
public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {

}
