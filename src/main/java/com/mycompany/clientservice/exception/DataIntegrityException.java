package com.mycompany.clientservice.exception;

public class DataIntegrityException extends RuntimeException{
    public DataIntegrityException(String message) {
        super(message);
    }
}
