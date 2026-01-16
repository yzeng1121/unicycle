package com.unicycle.exception;

public class FailedToExtractUserIdException extends RuntimeException {
    public FailedToExtractUserIdException(String message) {
        super(message);
    }
}

