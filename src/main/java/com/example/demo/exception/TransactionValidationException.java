package com.example.demo.exception;

public class TransactionValidationException extends RuntimeException {
    public TransactionValidationException(String message) {
        super(message);
    }
}
