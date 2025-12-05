package com.abc.social_service.client;

/**
 * Client interface for communicating with User Service to fetch user information
 */
public interface UserServiceClient {
    
    /**
     * Fetches user ELO rank from User Service
     * @param userId The user ID
     * @return ELO rank value, or default (1000) if unavailable
     */
    Integer getUserEloRank(Long userId);
    
    /**
     * Applies ELO points to a user
     * @param userId The user ID
     * @param action The action type (e.g., "VOTE_COMMENT")
     * @param points The points to apply
     * @param description Optional description
     */
    void applyEloPoints(Long userId, String action, Integer points, String description);
    
    /**
     * Checks if User Service is available
     * @return true if service is reachable
     */
    boolean isAvailable();
}
