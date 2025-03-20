package org.example.repository;

import org.example.data.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностями адресов.
 * Предоставляет методы для сохранения, получения, обновления и удаления адресов,
 * а также для получения адресов по различным критериям.
 */
public interface AddressRepository extends JpaRepository<AddressEntity, UUID> {

    /**
     * Получение всех адресов пользователя.
     *
     * @param userId Уникальный идентификатор пользователя
     * @return Набор адресов пользователя
     */
    Set<AddressEntity> findByUser_Id(UUID userId);

    /**
     * Получение адреса по умолчанию для пользователя.
     *
     * @param userId Уникальный идентификатор пользователя
     * @return Опционально возвращает адрес по умолчанию
     */
    Optional<AddressEntity> findByUser_IdAndIsDefaultTrue(UUID userId);

    /**
     * Получение любого адреса пользователя, кроме указанного.
     * Используется при удалении адреса по умолчанию.
     *
     * @param userId Уникальный идентификатор пользователя
     * @param addressId Уникальный идентификатор адреса для исключения
     * @return Опционально возвращает любой другой адрес пользователя
     */
    Optional<AddressEntity> findFirstByUser_IdAndIdNot(UUID userId, UUID addressId);

    /**
     * Подсчет количества адресов пользователя.
     *
     * @param userId Уникальный идентификатор пользователя
     * @return Количество адресов пользователя
     */
    long countByUser_Id(UUID userId);


}
