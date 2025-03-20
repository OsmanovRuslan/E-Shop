package org.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Обработчик исключений для всего приложения.
 * Перехватывает исключения и преобразует их в соответствующие HTTP-ответы.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает исключение InvalidCredentialsException.
     * Возвращает HTTP-статус 401 Unauthorized.
     *
     * @param exception Перехваченное исключение
     * @return ResponseEntity с сообщением об ошибке
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<String> handle(InvalidCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
    }

    /**
     * Обрабатывает исключения RuntimeException, LoginException и RegistrationException.
     * Возвращает HTTP-статус 500 Internal Server Error.
     *
     * @param exception Перехваченное исключение
     * @return ResponseEntity с сообщением об ошибке
     */
    @ExceptionHandler({RuntimeException.class, LoginException.class, RegistrationException.class})
    public ResponseEntity<String> handle(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
    }

    /**
     * Обрабатывает исключение UserAlreadyExistsException.
     * Возвращает HTTP-статус 409 Conflict.
     *
     * @param exception Перехваченное исключение
     * @return ResponseEntity с сообщением об ошибке
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> handle(UserAlreadyExistsException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }

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

}
