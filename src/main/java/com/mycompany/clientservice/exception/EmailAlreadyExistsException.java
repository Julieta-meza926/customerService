package com.mycompany.clientservice.exception;

public class EmailAlreadyExistsException extends RuntimeException{
public EmailAlreadyExistsException (String message){
    super(message );
}
}
