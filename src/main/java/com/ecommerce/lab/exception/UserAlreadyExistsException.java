package com.ecommerce.lab.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) { super(message); }
}
