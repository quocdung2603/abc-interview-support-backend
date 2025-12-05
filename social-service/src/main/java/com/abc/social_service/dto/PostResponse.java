package com.abc.social_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostResponse {
    private Long id;
    private Long userId;
    
    // Classification IDs
    private Long fieldId;
    private Long topicId;
    private Long levelId;
    
    private String fieldName;
    private String topicName;
    private String levelName;
    
    private String postType;   // DISCUSSION or QUESTION
    private String status;     // DRAFT, PUBLISHED, or LOCKED
    private String title;
    private String content;
    private String lockTime;   // ISO 8601 format
    private String createdAt;  // ISO 8601 format
    private String updatedAt;  // ISO 8601 format
}
