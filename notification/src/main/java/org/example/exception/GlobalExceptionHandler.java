package org.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Глобальный обработчик исключений для уведомлений.
 * Преобразует исключения в соответствующие HTTP-ответы.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает исключение NotificationNotFoundException.
     * Возвращает HTTP-статус 404 Not Found.
     *
     * @param exception Перехваченное исключение
     * @return ResponseEntity с сообщением об ошибке
     */
    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<String> handle(NotificationNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

}
