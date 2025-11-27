package com.abc.exam_service.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: exam-question-api-fixes, Property 2: Response excludes deprecated fields
 * 
 * Property-based test to verify that ExamResponse JSON does not contain deprecated fields.
 * Validates Requirements 1.3, 1.4
 */
public class ExamResponseFieldExclusionPropertyTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final Random random = new Random();

    /**
     * Property: For any exam response, when serialized to JSON,
     * the resulting JSON string should not contain the keys "topics" or "questionTypes".
     */
    @Test
    public void examResponseShouldNotContainDeprecatedFields() throws Exception {
        // Run property test with 100 iterations
        for (int i = 0; i < 100; i++) {
            ExamResponse response = generateRandomExamResponse();
            
            // Serialize to JSON
            String json = objectMapper.writeValueAsString(response);
            
            // Verify deprecated fields are not present in JSON
            assertFalse(json.contains("\"topics\""), 
                    "Iteration " + i + ": JSON should not contain 'topics' field");
            assertFalse(json.contains("\"questionTypes\""), 
                    "Iteration " + i + ": JSON should not contain 'questionTypes' field");
            
            // Verify new fields are present (if data exists)
            if (response.getTopicIds() != null && !response.getTopicIds().isEmpty()) {
                assertTrue(json.contains("\"topicIds\""), 
                        "Iteration " + i + ": JSON should contain 'topicIds' field");
            }
            if (response.getQuestionTypeIds() != null && !response.getQuestionTypeIds().isEmpty()) {
                assertTrue(json.contains("\"questionTypeIds\""), 
                        "Iteration " + i + ": JSON should contain 'questionTypeIds' field");
            }
        }
    }

    /**
     * Generate random ExamResponse for property testing
     */
    private ExamResponse generateRandomExamResponse() {
        ExamResponse response = new ExamResponse();
        response.setId((long) random.nextInt(1, 10000));
        response.setUserId((long) random.nextInt(1, 1000));
        response.setTitle("Test Exam " + random.nextInt(10000));
        response.setPosition("Position " + random.nextInt(100));
        response.setExamType(randomChoice("VIRTUAL", "RECRUITER", "PRACTICE"));
        response.setStatus(randomChoice("DRAFT", "PUBLISHED", "ONGOING", "COMPLETED"));
        response.setQuestionCount(random.nextInt(1, 100));
        response.setDuration(random.nextInt(10, 180));
        response.setLanguage(randomChoice("en", "vi", "fr", "de"));
        response.setCreatedAt(LocalDateTime.now());
        response.setCreatedBy((long) random.nextInt(1, 1000));
        
        // Generate random IDs
        response.setFieldId((long) random.nextInt(1, 10));
        response.setLevelId((long) random.nextInt(1, 5));
        
        // Generate random topic IDs (1-10 topics)
        int topicCount = random.nextInt(1, 11);
        List<Long> topicIds = new ArrayList<>();
        for (int i = 0; i < topicCount; i++) {
            topicIds.add((long) random.nextInt(1, 100));
        }
        response.setTopicIds(topicIds);
        
        // Generate random question type IDs (1-5 types)
        int typeCount = random.nextInt(1, 6);
        List<Long> questionTypeIds = new ArrayList<>();
        for (int i = 0; i < typeCount; i++) {
            questionTypeIds.add((long) random.nextInt(1, 10));
        }
        response.setQuestionTypeIds(questionTypeIds);
        
        return response;
    }
    
    private String randomChoice(String... options) {
        return options[random.nextInt(options.length)];
    }
}
