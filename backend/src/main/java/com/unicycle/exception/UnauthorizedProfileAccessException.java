package com.unicycle.exception;

public class UnauthorizedProfileAccessException extends RuntimeException {
    public UnauthorizedProfileAccessException(String message) {
        super(message);
    }
}

