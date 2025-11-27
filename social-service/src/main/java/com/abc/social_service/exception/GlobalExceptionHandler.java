package com.abc.social_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePostNotFoundException(PostNotFoundException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCommentNotFoundException(CommentNotFoundException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(PostLockedException.class)
    public ResponseEntity<Map<String, Object>> handlePostLockedException(PostLockedException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(DuplicateVoteException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateVoteException(DuplicateVoteException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(CommentLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleCommentLimitExceededException(CommentLimitExceededException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(EditLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleEditLimitExceededException(EditLimitExceededException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(InvalidVoteTypeException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidVoteTypeException(InvalidVoteTypeException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        StringBuilder message = new StringBuilder("Validation failed: ");
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            message.append(error.getField()).append(" - ").append(error.getDefaultMessage()).append("; ")
        );
        return buildErrorResponse(message.toString(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex, WebRequest request) {
        return buildErrorResponse("Internal server error: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, HttpStatus status, WebRequest request) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(errorResponse, status);
    }
}
