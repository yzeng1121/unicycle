package com.unicycle.exception;

public class InvalidBearerTokenException extends RuntimeException {
    public InvalidBearerTokenException(String message) {
        super(message);
    }
}
