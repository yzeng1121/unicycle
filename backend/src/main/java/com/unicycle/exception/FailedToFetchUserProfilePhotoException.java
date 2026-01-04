package com.unicycle.exception;

public class FailedToFetchUserProfilePhotoException extends RuntimeException {
    public FailedToFetchUserProfilePhotoException(String message) {
        super(message);
    }
}
