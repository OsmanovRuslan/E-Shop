package org.example.exception.address;

public class UnauthorizedAddressAccessException extends RuntimeException {

    public UnauthorizedAddressAccessException(String message) {
        super(message);
    }

    public UnauthorizedAddressAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
