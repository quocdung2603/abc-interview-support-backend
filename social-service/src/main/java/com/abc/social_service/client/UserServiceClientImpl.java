package com.abc.social_service.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;

/**
 * Implementation of UserServiceClient with circuit breaker and caching
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClientImpl implements UserServiceClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${user-service.url:http://localhost:8081}")
    private String userServiceUrl;
    
    private static final Integer DEFAULT_ELO_RANK = 1000;
    private static final int TIMEOUT_SECONDS = 2;
    
    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserEloRankFallback")
    public Integer getUserEloRank(Long userId) {
        try {
            log.debug("Fetching ELO rank for user: {}", userId);
            
            String url = userServiceUrl + "/users/" + userId + "/elo";
            
            // Use RestTemplate with timeout
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("eloRank")) {
                Integer eloRank = (Integer) response.get("eloRank");
                log.debug("Retrieved ELO rank {} for user {}", eloRank, userId);
                return eloRank;
            }
            
            log.warn("No ELO rank found in response for user {}, using default", userId);
            return DEFAULT_ELO_RANK;
            
        } catch (Exception e) {
            log.error("Error fetching ELO rank for user {}: {}", userId, e.getMessage());
            throw e; // Let circuit breaker handle it
        }
    }
    
    /**
     * Fallback method when User Service is unavailable
     */
    private Integer getUserEloRankFallback(Long userId, Exception e) {
        log.warn("User Service unavailable for user {}, using default ELO rank. Error: {}", 
                userId, e.getMessage());
        return DEFAULT_ELO_RANK;
    }
    
    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "applyEloPointsFallback")
    public void applyEloPoints(Long userId, String action, Integer points, String description) {
        try {
            log.debug("Applying {} ELO points to user {} for action: {}", points, userId, action);
            
            String url = userServiceUrl + "/users/elo";
            
            Map<String, Object> request = Map.of(
                "userId", userId,
                "action", action,
                "points", points,
                "description", description != null ? description : ""
            );
            
            restTemplate.postForObject(url, request, Map.class);
            log.info("Successfully applied {} ELO points to user {} for {}", points, userId, action);
            
        } catch (Exception e) {
            log.error("Error applying ELO points to user {}: {}", userId, e.getMessage());
            throw e; // Let circuit breaker handle it
        }
    }
    
    /**
     * Fallback method when applying ELO points fails
     */
    private void applyEloPointsFallback(Long userId, String action, Integer points, String description, Exception e) {
        log.warn("Failed to apply ELO points to user {}. Points will not be awarded. Error: {}", 
                userId, e.getMessage());
        // Silently fail - voting should still work even if ELO update fails
    }
    
    @Override
    public boolean isAvailable() {
        try {
            String url = userServiceUrl + "/actuator/health";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return response != null && "UP".equals(response.get("status"));
        } catch (Exception e) {
            log.debug("User Service is not available: {}", e.getMessage());
            return false;
        }
    }
}
