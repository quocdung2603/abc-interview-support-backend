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
     * Checks if User Service is available
     * @return true if service is reachable
     */
    boolean isAvailable();
}
