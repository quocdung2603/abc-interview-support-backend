package com.abc.social_service.exception;

public class PostLockedException extends RuntimeException {
    public PostLockedException() {
        super("Post is locked, no new comments allowed");
    }
}
