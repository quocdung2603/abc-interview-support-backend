package com.abc.social_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VoteRequest {
    private Long commentId;  // Set from path parameter, not required in body
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Vote type is required")
    private String voteType;  // "USEFUL" or "NOT_USEFUL"
}
