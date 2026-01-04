package com.unicycle.exception;

public class FailedToDeleteListingException extends RuntimeException {
    public FailedToDeleteListingException(String message) {
        super(message);
    }
}

