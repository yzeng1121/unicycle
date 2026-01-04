package com.unicycle.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Authentication Related Exceptions

    @ExceptionHandler(InvalidCredentialsException.class) 
    public ResponseEntity<?> handleInvalidCredentials(InvalidCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("message", "Invalid email or password."));
    }

    @ExceptionHandler(ExpiredVerificationException.class)
    public ResponseEntity<?> handleExpiredVerificationException(ExpiredVerificationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(InvalidVerificationException.class)
    public ResponseEntity<?> handleInvalidVerificationException(InvalidVerificationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(UserAlreadyVerifiedException.class)
    public ResponseEntity<?> handleUserAlreadyVerifiedException(UserAlreadyVerifiedException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<?> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(UserNotVerifiedException.class)
    public ResponseEntity<?> handleUserNotVerifiedException(UserNotVerifiedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException e) {
        return ResponseEntity.badRequest()
            .body(Map.of("message", "Invalid data."));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<?> handleEmailAlreadyExistsException(EmailAlreadyExistsException e) {
        return ResponseEntity.badRequest()
            .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(InvalidBearerTokenException.class)
    public ResponseEntity<?> handleInvalidBearerTokenException(InvalidBearerTokenException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("message", e.getMessage()));
    }

    // Image Uploading Exceptions 

    @ExceptionHandler(ImageUploadingException.class)
    public ResponseEntity<?> handleImageUploadingException(ImageUploadingException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of(
                "message", e.getMessage(), 
                "cause", e.getCause()
            ));
    }

    @ExceptionHandler(UnspecifiedImageFolderException.class)
    public ResponseEntity<?> handleUnspecifiedImageFolderException(UnspecifiedImageFolderException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(InvalidImageContentsException.class)
    public ResponseEntity<?> handleInvalidImageContentsException(InvalidImageContentsException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("message", e.getMessage()));
    }

    // Listing Related Exceptions

    @ExceptionHandler(FailedToCreateListingException.class)
    public ResponseEntity<?> handleFailedToCreateListingException(FailedToCreateListingException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(FailedToFetchListingException.class)
    public ResponseEntity<?> handleFailedToFetchListingException(FailedToFetchListingException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(FailedToFetchCoverPhotoException.class)
    public ResponseEntity<?> handleFailedToFetchCoverPhotoException(FailedToFetchCoverPhotoException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(ListingNotFoundException.class)
    public ResponseEntity<?> handleListingNotFoundException(ListingNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(UnauthorizedListingAccessException.class)
    public ResponseEntity<?> handleUnauthorizedListingAccessException(UnauthorizedListingAccessException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(FailedToDeleteListingException.class)
    public ResponseEntity<?> handleFailedToDeleteListingException(FailedToDeleteListingException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("message", e.getMessage()));
    }

    // Profile Related Exceptions
    
    @ExceptionHandler(FailedToCreateProfileException.class)
    public ResponseEntity<?> handleFailedToCreateProfileException(FailedToCreateProfileException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(FailedToFetchProfileException.class)
    public ResponseEntity<?> handleFailedToFetchProfileException(FailedToFetchProfileException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(FailedToFetchUserProfilePhotoException.class)
    public ResponseEntity<?> handleFailedToFetchUserProfilePhotoException(FailedToFetchUserProfilePhotoException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(UnauthorizedProfileAccessException.class)
    public ResponseEntity<?> handleUnauthorizedProfileAccessException(UnauthorizedProfileAccessException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("message", e.getMessage()));
    }
}
