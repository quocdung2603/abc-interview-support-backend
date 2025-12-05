package com.abc.social_service.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class InvalidClassificationException extends RuntimeException {
    private final Map<String, String> fieldErrors;
    
    public InvalidClassificationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }
}
