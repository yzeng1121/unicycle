package com.unicycle.exception;

public class FailedToFetchUsernameException extends RuntimeException {
    public FailedToFetchUsernameException(String message) {
        super(message);
    }
}
