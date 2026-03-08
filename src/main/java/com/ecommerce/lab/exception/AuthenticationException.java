package com.ecommerce.lab.exception;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) { super(message); }
}