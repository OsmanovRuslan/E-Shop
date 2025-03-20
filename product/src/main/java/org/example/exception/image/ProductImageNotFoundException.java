package org.example.exception.image;

import org.example.exception.NotFoundException;

public class ProductImageNotFoundException extends NotFoundException {

    public ProductImageNotFoundException(String message) {
        super(message);
    }

    public ProductImageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
