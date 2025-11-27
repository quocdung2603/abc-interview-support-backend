package com.abc.social_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentUpdateRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotBlank(message = "Content is required")
    private String content;
}
