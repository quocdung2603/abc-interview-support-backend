package com.abc.social_service.exception;

public class DuplicateVoteException extends RuntimeException {
    public DuplicateVoteException() {
        super("User has already voted on this comment");
    }
}
