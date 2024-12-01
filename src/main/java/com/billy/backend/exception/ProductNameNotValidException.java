package com.billy.backend.exception;

public class ProductNameNotValidException extends RuntimeException {
    public ProductNameNotValidException(String message){
        super(message);
    }
}
