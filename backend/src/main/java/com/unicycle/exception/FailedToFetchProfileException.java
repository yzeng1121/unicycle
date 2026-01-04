package com.unicycle.exception;

public class FailedToFetchProfileException extends RuntimeException {
    public FailedToFetchProfileException(String message) {
        super(message);
    }
}
