package org.example.exception.feign;

public class DataGettingException extends RuntimeException {

    public DataGettingException(String message) {
        super(message);
    }

    public DataGettingException(String message, Throwable cause) {
        super(message, cause);
    }

}
