package org.example.exception;

import org.example.exception.address.AddressNotFoundException;
import org.example.exception.address.UnauthorizedAddressAccessException;
import org.example.exception.user.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Глобальный обработчик исключений для всего приложения.
 * Преобразует исключения в соответствующие HTTP-ответы.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает исключение UserNotFoundException.
     * Возвращает HTTP-статус 404 Not Found.
     *
     * @param exception Перехваченное исключение
     * @return ResponseEntity с сообщением об ошибке
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handle(UserNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    /**
     * Обрабатывает исключение AddressNotFoundException.
     * Возвращает HTTP-статус 404 Not Found.
     *
     * @param exception Перехваченное исключение
     * @return ResponseEntity с сообщением об ошибке
     */
    @ExceptionHandler(AddressNotFoundException.class)
    public ResponseEntity<String> handle(AddressNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    /**
     * Обрабатывает исключение UnauthorizedAddressAccessException.
     * Возвращает HTTP-статус 403 Forbidden.
     *
     * @param exception Перехваченное исключение
     * @return ResponseEntity с сообщением об ошибке
     */
    @ExceptionHandler(UnauthorizedAddressAccessException.class)
    public ResponseEntity<String> handle(UnauthorizedAddressAccessException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception.getMessage());
    }

}
