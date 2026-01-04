package com.unicycle.exception;

public class ImageUploadingException extends RuntimeException {
    public ImageUploadingException(String message, Throwable cause) {
        super(message, cause);
    }
}