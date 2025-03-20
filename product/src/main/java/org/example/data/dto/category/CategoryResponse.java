package org.example.data.dto.category;

import java.util.UUID;

/**
 * DTO для ответа с информацией о категории.
 *
 * @param id Уникальный идентификатор категории
 * @param name Название категории
 * @param description Описание категории
 */
public record CategoryResponse (

        UUID id,

        String name,

        String description

) {}