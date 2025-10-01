package com.mycompany.clientservice.exception;
import org.springframework.http.HttpStatus;

public class JwtAuthException extends RuntimeException{
    public JwtAuthException(String message) {
        super(message);
    }
}

