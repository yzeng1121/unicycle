package com.unicycle.exception;

public class FailedToFetchUserIdException extends RuntimeException {
    public FailedToFetchUserIdException(String message) {
        super(message);
    }
}
