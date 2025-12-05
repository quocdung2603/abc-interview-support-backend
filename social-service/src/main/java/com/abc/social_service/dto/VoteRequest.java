package com.abc.social_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VoteRequest {
    private Long commentId;  // Set from path parameter, not required in body
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private String voteType;  // "USEFUL" or "NOT_USEFUL" (for backward compatibility)
    
    private Boolean useful;    // true for useful vote
    private Boolean unuseful;  // true for unuseful vote
    
    /**
     * Get the vote type from either voteType field or useful/unuseful flags
     * Priority: useful/unuseful flags > voteType field
     */
    public String getEffectiveVoteType() {
        if (useful != null && useful) {
            return "USEFUL";
        }
        if (unuseful != null && unuseful) {
            return "NOT_USEFUL";
        }
        return voteType;
    }
}
