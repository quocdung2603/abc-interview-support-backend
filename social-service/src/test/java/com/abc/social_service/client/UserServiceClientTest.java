package com.abc.social_service.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    private UserServiceClientImpl userServiceClient;

    @BeforeEach
    void setUp() {
        userServiceClient = new UserServiceClientImpl(restTemplate);
    }

    @Test
    void getUserEloRank_Success() {
        // Given
        Long userId = 1L;
        Integer expectedEloRank = 1500;
        Map<String, Object> response = Map.of("eloRank", expectedEloRank);
        
        when(restTemplate.getForObject(any(String.class), eq(Map.class)))
                .thenReturn(response);

        // When
        Integer actualEloRank = userServiceClient.getUserEloRank(userId);

        // Then
        assertThat(actualEloRank).isEqualTo(expectedEloRank);
    }

    @Test
    void getUserEloRank_ServiceUnavailable_ReturnsDefault() {
        // Given
        Long userId = 1L;
        when(restTemplate.getForObject(any(String.class), eq(Map.class)))
                .thenThrow(new RestClientException("Service unavailable"));

        // When
        Integer actualEloRank = userServiceClient.getUserEloRank(userId);

        // Then - fallback should return default
        assertThat(actualEloRank).isEqualTo(1000);
    }

    @Test
    void getUserEloRank_Timeout_ReturnsDefault() {
        // Given
        Long userId = 1L;
        when(restTemplate.getForObject(any(String.class), eq(Map.class)))
                .thenThrow(new RestClientException("Timeout"));

        // When
        Integer actualEloRank = userServiceClient.getUserEloRank(userId);

        // Then
        assertThat(actualEloRank).isEqualTo(1000);
    }

    @Test
    void isAvailable_ServiceUp_ReturnsTrue() {
        // Given
        Map<String, Object> healthResponse = Map.of("status", "UP");
        when(restTemplate.getForObject(any(String.class), eq(Map.class)))
                .thenReturn(healthResponse);

        // When
        boolean available = userServiceClient.isAvailable();

        // Then
        assertThat(available).isTrue();
    }

    @Test
    void isAvailable_ServiceDown_ReturnsFalse() {
        // Given
        when(restTemplate.getForObject(any(String.class), eq(Map.class)))
                .thenThrow(new RestClientException("Connection refused"));

        // When
        boolean available = userServiceClient.isAvailable();

        // Then
        assertThat(available).isFalse();
    }
}
