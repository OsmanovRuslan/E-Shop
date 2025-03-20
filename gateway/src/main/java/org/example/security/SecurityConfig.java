package org.example.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

/**
 * Конфигурация безопасности для API микросервиса.
 * Настраивает доступ к различным эндпоинтам на основе ролей пользователей.
 */
@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final KeycloakRoleConverter keycloakRoleConverter;

    /**
     * Настраивает цепочку фильтров безопасности для входящих HTTP-запросов.
     * Определяет правила авторизации для различных эндпоинтов API.
     *
     * @param serverHttpSecurity Конфигурация HTTP-безопасности сервера
     * @return Настроенная цепочка фильтров безопасности
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity serverHttpSecurity) {
        serverHttpSecurity
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/api/v1/auth/**").permitAll()

                        .pathMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/products/**").hasAnyRole("MANAGER", "ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/products/**").hasAnyRole("MANAGER", "ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasAnyRole("MANAGER", "ADMIN")

                        .pathMatchers(HttpMethod.DELETE, "/api/v1/orders/**").hasAnyRole("MANAGER", "ADMIN")

                        .pathMatchers(HttpMethod.GET, "/api/v1/users/**").authenticated()
                        .pathMatchers(HttpMethod.PUT, "/api/v1/users/**").authenticated()
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("ADMIN")

                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance());

        return serverHttpSecurity.build();
    }

    /**
     * Создает конвертер для преобразования JWT-токена в объект аутентификации.
     * Использует KeycloakRoleConverter для извлечения ролей из токена.
     *
     * @return Конвертер JWT-аутентификации
     */
    @Bean
    public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(keycloakRoleConverter);
        return converter;
    }

    /**
     * Создает декодер для проверки JWT-токенов.
     *
     * @param jwkSetUri URI издателя JWT-токенов
     * @return Реактивный декодер JWT-токенов
     */
    @Bean
    public ReactiveJwtDecoder jwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String jwkSetUri) {
        return ReactiveJwtDecoders.fromIssuerLocation(jwkSetUri);
    }

}
