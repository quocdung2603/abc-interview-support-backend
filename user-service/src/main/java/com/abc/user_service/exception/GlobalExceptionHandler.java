package com.abc.user_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, 
            HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .type("https://errors.abc.com/USER_NOT_FOUND")
                .title("Resource Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .errorCode("USER_NOT_FOUND")
                .traceId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
            DuplicateResourceException ex,
            HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .type("https://errors.abc.com/USER_ALREADY_EXISTS")
                .title("Duplicate Resource")
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .errorCode("USER_ALREADY_EXISTS")
                .traceId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestException(
            InvalidRequestException ex,
            HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .type("https://errors.abc.com/INVALID_REQUEST")
                .title("Invalid Request")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .errorCode("INVALID_REQUEST")
                .traceId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .details(ex.getDetails())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ErrorResponse error = ErrorResponse.builder()
                .type("https://errors.abc.com/VALIDATION_FAILED")
                .title("Validation Failed")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Input validation failed. Please check the provided fields.")
                .instance(request.getRequestURI())
                .errorCode("VALIDATION_FAILED")
                .traceId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .details(validationErrors)
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .type("https://errors.abc.com/ACCESS_DENIED")
                .title("Access Denied")
                .status(HttpStatus.FORBIDDEN.value())
                .detail("You don't have permission to access this resource")
                .instance(request.getRequestURI())
                .errorCode("ACCESS_DENIED")
                .traceId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .type("https://errors.abc.com/" + ex.getErrorCode())
                .title("Business Rule Violation")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .errorCode(ex.getErrorCode())
                .traceId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .type("https://errors.abc.com/INVALID_ARGUMENT")
                .title("Invalid Argument")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .errorCode("INVALID_ARGUMENT")
                .traceId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex,
            HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .type("https://errors.abc.com/ILLEGAL_STATE")
                .title("Illegal State")
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .errorCode("ILLEGAL_STATE")
                .traceId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {
        // Log the full exception for debugging
        ex.printStackTrace();
        System.err.println("=== CAUGHT EXCEPTION IN GLOBAL HANDLER ===");
        System.err.println("Type: " + ex.getClass().getName());
        System.err.println("Message: " + ex.getMessage());
        System.err.println("Cause: " + (ex.getCause() != null ? ex.getCause().getMessage() : "null"));
        
        ErrorResponse error = ErrorResponse.builder()
                .type("https://errors.abc.com/INTERNAL_SERVER_ERROR")
                .title("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("An unexpected error occurred. Please contact support if the problem persists.")
                .instance(request.getRequestURI())
                .errorCode("INTERNAL_SERVER_ERROR")
                .traceId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
