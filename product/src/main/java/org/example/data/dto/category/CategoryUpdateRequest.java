package org.example.data.dto.category;

/**
 * DTO для запроса на обновление категории.
 *
 * @param name Новое название категории
 * @param description Новое описание категории
 */
public record CategoryUpdateRequest (

        String name,

        String description

) {}