package com.abc.social_service.exception;

public class CommentLimitExceededException extends RuntimeException {
    public CommentLimitExceededException() {
        super("User has already commented on this locked post");
    }
    
    public CommentLimitExceededException(String message) {
        super(message);
    }
}
