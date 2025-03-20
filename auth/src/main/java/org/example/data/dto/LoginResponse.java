package org.example.data.dto;

/**
 * DTO для ответа на запрос входа, содержащий токены доступа.
 *
 * @param accessToken Токен доступа
 * @param refreshToken Токен обновления
 * @param expiresIn Срок действия токена в секундах
 * @param tokenType Тип токена (например, "Bearer")
 */
public record LoginResponse(

        String accessToken,

        String refreshToken,

        Long expiresIn,

        String tokenType
) {}