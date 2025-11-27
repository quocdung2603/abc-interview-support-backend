package com.abc.social_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostResponse {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private LocalDateTime lockTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
