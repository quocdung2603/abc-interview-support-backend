package com.abc.social_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostRequest {
    // userId will be extracted from authentication context, not from request body
    
    @NotNull(message = "Field ID is required")
    private Long fieldId;
    
    @NotNull(message = "Topic ID is required")
    private Long topicId;
    
    private Long levelId;  // Optional
    
    @NotBlank(message = "Post type is required")
    private String postType;  // DISCUSSION or QUESTION
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    @NotBlank(message = "Content is required")
    @Size(max = 10000, message = "Content must not exceed 10000 characters")
    private String content;
    
    private LocalDateTime lockTime;  // Optional
}
