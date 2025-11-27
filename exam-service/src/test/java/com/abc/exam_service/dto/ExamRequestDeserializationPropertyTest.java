package com.abc.exam_service.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: exam-question-api-fixes, Property 3: Request deserialization accepts new fields
 * 
 * Property-based test to verify that ExamRequest correctly deserializes JSON with new field names.
 * Validates Requirements 2.5
 */
public class ExamRequestDeserializationPropertyTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    /**
     * Property: For any valid JSON request containing topicIds and questionTypeIds fields,
     * deserializing to ExamRequest should successfully populate those fields with the correct values.
     */
    @Test
    public void examRequestShouldDeserializeNewFields() throws Exception {
        // Run property test with 100 iterations
        for (int i = 0; i < 100; i++) {
            // Generate random data
            List<Long> expectedTopicIds = generateRandomIds(1, 10);
            List<Long> expectedQuestionTypeIds = generateRandomIds(1, 5);
            
            // Create JSON with new field names
            String json = String.format(
                "{\"userId\":1,\"examType\":\"VIRTUAL\",\"title\":\"Test Exam\",\"position\":\"Developer\"," +
                "\"fieldId\":1,\"topicIds\":%s,\"levelId\":2,\"questionTypeIds\":%s," +
                "\"questionCount\":20,\"duration\":60,\"language\":\"en\"}",
                objectMapper.writeValueAsString(expectedTopicIds),
                objectMapper.writeValueAsString(expectedQuestionTypeIds)
            );
            
            // Deserialize
            ExamRequest request = objectMapper.readValue(json, ExamRequest.class);
            
            // Verify fields are populated correctly
            assertNotNull(request.getTopicIds(), 
                    "Iteration " + i + ": topicIds should not be null");
            assertNotNull(request.getQuestionTypeIds(), 
                    "Iteration " + i + ": questionTypeIds should not be null");
            
            assertEquals(expectedTopicIds.size(), request.getTopicIds().size(),
                    "Iteration " + i + ": topicIds size should match");
            assertEquals(expectedQuestionTypeIds.size(), request.getQuestionTypeIds().size(),
                    "Iteration " + i + ": questionTypeIds size should match");
            
            // Verify values match
            for (int j = 0; j < expectedTopicIds.size(); j++) {
                assertEquals(expectedTopicIds.get(j), request.getTopicIds().get(j),
                        "Iteration " + i + ": topicIds[" + j + "] should match");
            }
            
            for (int j = 0; j < expectedQuestionTypeIds.size(); j++) {
                assertEquals(expectedQuestionTypeIds.get(j), request.getQuestionTypeIds().get(j),
                        "Iteration " + i + ": questionTypeIds[" + j + "] should match");
            }
        }
    }

    /**
     * Test that JSON without deprecated fields deserializes successfully
     */
    @Test
    public void examRequestShouldNotRequireDeprecatedFields() throws Exception {
        String json = "{\"userId\":1,\"examType\":\"VIRTUAL\",\"title\":\"Test Exam\"," +
                "\"topicIds\":[1,2,3],\"questionTypeIds\":[1,2]," +
                "\"questionCount\":20,\"duration\":60,\"language\":\"en\"}";
        
        ExamRequest request = objectMapper.readValue(json, ExamRequest.class);
        
        assertNotNull(request);
        assertEquals(3, request.getTopicIds().size());
        assertEquals(2, request.getQuestionTypeIds().size());
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
