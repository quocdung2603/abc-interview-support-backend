package com.abc.social_service.post;

import com.abc.social_service.dto.PostResponse;
import com.abc.social_service.entity.Post;
import com.abc.social_service.mapper.PostMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: exam-registration-post-improvements, Property 5: Post response contains all required fields
 * 
 * Property-based test to verify that PostResponse contains all required fields.
 * Validates Requirements 3.1, 3.2
 */
@SpringBootTest
public class PostResponseCompletenessPropertyTest {

    @Autowired
    private PostMapper postMapper;

    private final Random random = new Random();

    /**
     * Property: For any post returned by the API, the response should include all fields
     * specified in the Post interface: id, userId, fieldId, topicId, levelId, postType,
     * status, title, content, lockTime, createdAt, and updatedAt.
     */
    @Test
    public void postResponseShouldContainAllRequiredFields() {
        // Run property test with 100 iterations
        for (int i = 0; i < 100; i++) {
            // Generate random post
            Post post = createRandomPost();
            
            // Map to response
            PostResponse response = postMapper.toResponse(post);
            
            // Verify all required fields are present
            assertNotNull(response.getId(),
                    "Iteration " + i + ": id should not be null");
            assertNotNull(response.getUserId(),
                    "Iteration " + i + ": userId should not be null");
            assertNotNull(response.getFieldId(),
                    "Iteration " + i + ": fieldId should not be null");
            assertNotNull(response.getTopicId(),
                    "Iteration " + i + ": topicId should not be null");
            // levelId can be null (optional)
            assertNotNull(response.getPostType(),
                    "Iteration " + i + ": postType should not be null");
            assertNotNull(response.getStatus(),
                    "Iteration " + i + ": status should not be null");
            assertNotNull(response.getTitle(),
                    "Iteration " + i + ": title should not be null");
            assertNotNull(response.getContent(),
                    "Iteration " + i + ": content should not be null");
            // lockTime can be null (optional)
            assertNotNull(response.getCreatedAt(),
                    "Iteration " + i + ": createdAt should not be null");
            assertNotNull(response.getUpdatedAt(),
                    "Iteration " + i + ": updatedAt should not be null");
            
            // Verify values match
            assertEquals(post.getId(), response.getId(),
                    "Iteration " + i + ": id should match");
            assertEquals(post.getUserId(), response.getUserId(),
                    "Iteration " + i + ": userId should match");
            assertEquals(post.getFieldId(), response.getFieldId(),
                    "Iteration " + i + ": fieldId should match");
            assertEquals(post.getTopicId(), response.getTopicId(),
                    "Iteration " + i + ": topicId should match");
            assertEquals(post.getLevelId(), response.getLevelId(),
                    "Iteration " + i + ": levelId should match");
            assertEquals(post.getPostType(), response.getPostType(),
                    "Iteration " + i + ": postType should match");
            assertEquals(post.getStatus(), response.getStatus(),
                    "Iteration " + i + ": status should match");
            assertEquals(post.getTitle(), response.getTitle(),
                    "Iteration " + i + ": title should match");
            assertEquals(post.getContent(), response.getContent(),
                    "Iteration " + i + ": content should match");
        }
    }

    private Post createRandomPost() {
        Post post = new Post();
        post.setId((long) random.nextInt(1, 10000));
        post.setUserId((long) random.nextInt(1, 1000));
        post.setFieldId((long) random.nextInt(1, 10));
        post.setTopicId((long) random.nextInt(1, 50));
        
        // levelId is optional - 50% chance of being null
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
        
        post.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(1, 365)));
        post.setUpdatedAt(LocalDateTime.now().minusDays(random.nextInt(0, 30)));
        
        return post;
    }

    private String getRandomStatus() {
        String[] statuses = {"DRAFT", "PUBLISHED", "LOCKED"};
        return statuses[random.nextInt(statuses.length)];
    }
}
