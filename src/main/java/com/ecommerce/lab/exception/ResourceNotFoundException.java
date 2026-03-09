package com.ecommerce.lab.exception;

// For anything that simply doesn't exist (User, Category, Gift Card, Token)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}
