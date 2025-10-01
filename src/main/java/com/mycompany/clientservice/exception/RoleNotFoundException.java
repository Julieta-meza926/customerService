package com.mycompany.clientservice.exception;
import org.springframework.http.HttpStatus;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String message) {
        super(message);
    }
}
