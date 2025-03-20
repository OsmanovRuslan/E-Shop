package org.example.data.dto.category;

/**
 * DTO для запроса на создание категории.
 *
 * @param name Название категории
 * @param description Описание категории
 */
public record CategoryCreateRequest (

        String name,

        String description

) {}