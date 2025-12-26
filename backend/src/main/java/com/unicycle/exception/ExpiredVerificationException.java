package com.unicycle.exception;

public class ExpiredVerificationException extends RuntimeException {
    public ExpiredVerificationException(String message) {
        super(message);
    }
}

