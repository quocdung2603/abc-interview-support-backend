package com.abc.social_service.exception;

public class InvalidVoteTypeException extends RuntimeException {
    public InvalidVoteTypeException() {
        super("Vote type must be USEFUL or NOT_USEFUL");
    }
    
    public InvalidVoteTypeException(String message) {
        super(message);
    }
}
