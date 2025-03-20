package org.example.repository;

import org.example.data.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностями пользователей.
 * Предоставляет методы для сохранения, получения, обновления и удаления пользователей.
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

}