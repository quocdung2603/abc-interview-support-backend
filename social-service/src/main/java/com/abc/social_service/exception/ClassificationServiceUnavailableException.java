package com.abc.social_service.exception;

public class ClassificationServiceUnavailableException extends RuntimeException {
    public ClassificationServiceUnavailableException(String message) {
        super(message);
    }
    
    public ClassificationServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
