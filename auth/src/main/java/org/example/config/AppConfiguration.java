package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Основная конфигурация приложения.
 * Предоставляет необходимые бины для работы сервиса.
 */
@Configuration
public class AppConfiguration {

    /**
     * Создает экземпляр RestTemplate для выполнения HTTP-запросов.
     *
     * @return Настроенный экземпляр RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
