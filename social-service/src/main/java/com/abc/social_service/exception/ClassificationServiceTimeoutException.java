package com.abc.social_service.exception;

public class ClassificationServiceTimeoutException extends RuntimeException {
    public ClassificationServiceTimeoutException(String message) {
        super(message);
    }
    
    public ClassificationServiceTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
