package com.abc.social_service.post;

import com.abc.social_service.dto.PostResponse;
import com.abc.social_service.entity.Post;
import com.abc.social_service.mapper.PostMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: exam-registration-post-improvements, Property 6: Timestamps are ISO 8601 formatted
 * 
 * Property-based test to verify that timestamps in PostResponse are ISO 8601 formatted.
 * Validates Requirements 3.4, 3.5
 */
@SpringBootTest
public class PostTimestampFormattingPropertyTest {

    @Autowired
    private PostMapper postMapper;

    private final Random random = new Random();

    /**
     * Property: For any post response, the lockTime, createdAt, and updatedAt fields
     * (when non-null) should be valid ISO 8601 formatted strings.
     */
    @Test
    public void postTimestampsShouldBeISO8601Formatted() {
        // Run property test with 100 iterations
        for (int i = 0; i < 100; i++) {
            // Generate random post
            Post post = createRandomPost();
            
            // Map to response
            PostResponse response = postMapper.toResponse(post);
            
            // Verify createdAt is ISO 8601 formatted
            assertNotNull(response.getCreatedAt(),
                    "Iteration " + i + ": createdAt should not be null");
            assertTrue(isValidISO8601(response.getCreatedAt()),
                    "Iteration " + i + ": createdAt should be valid ISO 8601 format: " + response.getCreatedAt());
            
            // Verify updatedAt is ISO 8601 formatted
            assertNotNull(response.getUpdatedAt(),
                    "Iteration " + i + ": updatedAt should not be null");
            assertTrue(isValidISO8601(response.getUpdatedAt()),
                    "Iteration " + i + ": updatedAt should be valid ISO 8601 format: " + response.getUpdatedAt());
            
            // Verify lockTime is ISO 8601 formatted (if present)
            if (response.getLockTime() != null) {
                assertTrue(isValidISO8601(response.getLockTime()),
                        "Iteration " + i + ": lockTime should be valid ISO 8601 format: " + response.getLockTime());
            }
            
            // Verify timestamps can be parsed back to LocalDateTime
            LocalDateTime parsedCreatedAt = parseISO8601(response.getCreatedAt());
            assertNotNull(parsedCreatedAt,
                    "Iteration " + i + ": createdAt should be parseable");
            
            LocalDateTime parsedUpdatedAt = parseISO8601(response.getUpdatedAt());
            assertNotNull(parsedUpdatedAt,
                    "Iteration " + i + ": updatedAt should be parseable");
            
            if (response.getLockTime() != null) {
                LocalDateTime parsedLockTime = parseISO8601(response.getLockTime());
                assertNotNull(parsedLockTime,
                        "Iteration " + i + ": lockTime should be parseable");
            }
            
            // Verify parsed values match original (within same second due to formatting)
            assertEquals(post.getCreatedAt().withNano(0), parsedCreatedAt.withNano(0),
                    "Iteration " + i + ": parsed createdAt should match original");
            assertEquals(post.getUpdatedAt().withNano(0), parsedUpdatedAt.withNano(0),
                    "Iteration " + i + ": parsed updatedAt should match original");
        }
    }

    private boolean isValidISO8601(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return false;
        }
        try {
            LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private LocalDateTime parseISO8601(String dateTimeString) {
        try {
            return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private Post createRandomPost() {
        Post post = new Post();
        post.setId((long) random.nextInt(1, 10000));
        post.setUserId((long) random.nextInt(1, 1000));
        post.setFieldId((long) random.nextInt(1, 10));
        post.setTopicId((long) random.nextInt(1, 50));
        
        // levelId is optional
        if (random.nextBoolean()) {
            post.setLevelId((long) random.nextInt(1, 5));
        }
        
        post.setPostType(random.nextBoolean() ? "DISCUSSION" : "QUESTION");
        post.setStatus(getRandomStatus());
        post.setTitle("Test Post " + random.nextInt(10000));
        post.setContent("Test content " + random.nextInt(10000));
        
        // lockTime is optional - 30% chance of being set
        if (random.nextDouble() < 0.3) {
            post.setLockTime(LocalDateTime.now().plusDays(random.nextInt(1, 30)));
        }
        
        // Generate random timestamps
        post.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(1, 365)));
        post.setUpdatedAt(LocalDateTime.now().minusDays(random.nextInt(0, 30)));
        
        return post;
    }

    private String getRandomStatus() {
        String[] statuses = {"DRAFT", "PUBLISHED", "LOCKED"};
        return statuses[random.nextInt(statuses.length)];
    }
}
