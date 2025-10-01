package com.mycompany.clientservice.exception;

public class CacheException extends RuntimeException {
    public CacheException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }}