package org.example.repository;

import org.example.data.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностями товаров.
 * Предоставляет методы для сохранения, получения, обновления и удаления товаров,
 * а также для поиска и фильтрации товаров по различным критериям.
 */
@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {

    /**
     * Поиск товаров по названию или описанию, содержащим указанный текст.
     * Поиск выполняется без учета регистра.
     *
     * @param searchPattern Шаблон поиска в формате '%текст%'
     * @return Список найденных сущностей товаров
     */
    @Query("SELECT p FROM ProductEntity p WHERE " +
            "LOWER(p.name) LIKE :searchPattern OR " +
            "LOWER(p.description) LIKE :searchPattern")
    List<ProductEntity> searchByNameOrDescription(@Param("searchPattern") String searchPattern);

    /**
     * Поиск товаров по указанной категории.
     *
     * @param categoryId Уникальный идентификатор категории
     * @return Список сущностей товаров, принадлежащих к указанной категории
     */
    @Query("SELECT p FROM ProductEntity p JOIN p.categories c WHERE c.id = :categoryId")
    List<ProductEntity> findByCategoryId(@Param("categoryId") UUID categoryId);

}