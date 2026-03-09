package com.ecommerce.lab.exception;

// For logic errors (Out of stock, Expired, Empty cart, Invalid password)
public class BusinessLogicException extends RuntimeException {
    public BusinessLogicException(String message) { super(message); }
}