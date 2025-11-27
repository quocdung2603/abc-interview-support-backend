package com.abc.social_service.exception;

public class EditLimitExceededException extends RuntimeException {
    public EditLimitExceededException() {
        super("Comment has already been edited once and cannot be edited again");
    }
    
    public EditLimitExceededException(String message) {
        super(message);
    }
}
