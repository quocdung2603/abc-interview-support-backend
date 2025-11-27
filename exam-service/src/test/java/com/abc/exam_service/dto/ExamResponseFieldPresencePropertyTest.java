package com.abc.exam_service.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: exam-question-api-fixes, Property 1: Response contains required fields
 * 
 * Property-based test to verify that ExamResponse contains topicIds and questionTypeIds.
 * Validates Requirements 1.1, 1.2
 */
public class ExamResponseFieldPresencePropertyTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    /**
     * Property: For any exam response with topicIds and questionTypeIds,
     * both fields should be present and contain the correct values.
     */
    @Test
    public void examResponseShouldContainRequiredFields() throws Exception {
        // Run property test with 100 iterations
        for (int i = 0; i < 100; i++) {
            // Generate random topic and question type IDs
            List<Long> expectedTopicIds = generateRandomIds(1, 10);
            List<Long> expectedQuestionTypeIds = generateRandomIds(1, 5);
            
            // Create response with these IDs
            ExamResponse response = new ExamResponse();
            response.setTopicIds(expectedTopicIds);
            response.setQuestionTypeIds(expectedQuestionTypeIds);
            
            // Verify required fields are present
            assertNotNull(response.getTopicIds(), 
                    "Iteration " + i + ": topicIds should not be null");
            assertNotNull(response.getQuestionTypeIds(), 
                    "Iteration " + i + ": questionTypeIds should not be null");
            
            // Verify values match
            assertEquals(expectedTopicIds.size(), response.getTopicIds().size(),
                    "Iteration " + i + ": topicIds size should match");
            assertEquals(expectedQuestionTypeIds.size(), response.getQuestionTypeIds().size(),
                    "Iteration " + i + ": questionTypeIds size should match");
            
            for (int j = 0; j < expectedTopicIds.size(); j++) {
                assertEquals(expectedTopicIds.get(j), response.getTopicIds().get(j),
                        "Iteration " + i + ": topicIds[" + j + "] should match");
            }
            
            for (int j = 0; j < expectedQuestionTypeIds.size(); j++) {
                assertEquals(expectedQuestionTypeIds.get(j), response.getQuestionTypeIds().get(j),
                        "Iteration " + i + ": questionTypeIds[" + j + "] should match");
            }
        }
    }

    private List<Long> generateRandomIds(int minCount, int maxCount) {
        int count = random.nextInt(minCount, maxCount + 1);
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add((long) random.nextInt(1, 100));
        }
        return ids;
    }
}
