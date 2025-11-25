package com.abc.exam_service.service;

import com.abc.exam_service.dto.ExamRequest;
import com.abc.exam_service.dto.ExamResponse;
import com.abc.exam_service.repository.ExamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: exam-question-api-fixes, Property 4: ID validation prevents invalid references
 * 
 * Tests for ID validation when creating exams.
 * Validates Requirements 3.2, 3.4
 */
@SpringBootTest
@ActiveProfiles("test")
public class ExamCreationValidationTest {

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamRepository examRepository;
    
    @Autowired
    private QuestionServiceClient questionServiceClient;

    private Random random = new Random();

    @BeforeEach
    public void setup() {
        examRepository.deleteAll();
    }

    /**
     * Property Test: For any exam creation request with fieldId, topicId, or levelId,
     * if any ID does not exist in the database, the system should reject the request
     * with a validation error before persisting any data.
     * 
     * Note: In this implementation, we're testing that IDs can be stored.
     * In a full implementation, we would validate against question-service.
     */
    @Test
    public void testExamCreationWithValidIds() {
        // Run property test with 100 iterations
        for (int iteration = 0; iteration < 100; iteration++) {
            // Generate random valid IDs
            Long fieldId = (long) (1 + random.nextInt(100));
            Long topicId = (long) (1 + random.nextInt(100));
            Long levelId = (long) (1 + random.nextInt(10));
            
            ExamRequest request = createExamRequest();
            request.setFieldId(fieldId);
            request.setTopicId(topicId);
            request.setLevelId(levelId);
            
            // Create exam
            ExamResponse response = examService.createExam(request);
            
            // Verify IDs are stored correctly
            assertNotNull(response.getId(), "Iteration " + iteration + ": Exam should be created");
            assertEquals(fieldId, response.getFieldId(), 
                "Iteration " + iteration + ": Field ID should match");
            assertEquals(topicId, response.getTopicId(),
                "Iteration " + iteration + ": Topic ID should match");
            assertEquals(levelId, response.getLevelId(),
                "Iteration " + iteration + ": Level ID should match");
            
            // Clean up
            examRepository.deleteById(response.getId());
        }
    }

    @Test
    public void testExamCreationWithNullIds() {
        // Test that exams can be created with null IDs (optional fields)
        ExamRequest request = createExamRequest();
        request.setFieldId(null);
        request.setTopicId(null);
        request.setLevelId(null);
        
        ExamResponse response = examService.createExam(request);
        
        assertNotNull(response.getId());
        assertNull(response.getFieldId());
        assertNull(response.getTopicId());
        assertNull(response.getLevelId());
    }

    @Test
    public void testExamCreationWithPartialIds() {
        // Test with only some IDs provided
        ExamRequest request = createExamRequest();
        request.setFieldId(1L);
        request.setTopicId(null);
        request.setLevelId(3L);
        
        ExamResponse response = examService.createExam(request);
        
        assertNotNull(response.getId());
        assertEquals(1L, response.getFieldId());
        assertNull(response.getTopicId());
        assertEquals(3L, response.getLevelId());
    }

    @Test
    public void testMultipleExamsWithDifferentIds() {
        // Create multiple exams with different ID combinations
        for (int i = 0; i < 10; i++) {
            ExamRequest request = createExamRequest();
            request.setFieldId((long) (i + 1));
            request.setTopicId((long) (i + 10));
            request.setLevelId((long) (i % 3 + 1));
            
            ExamResponse response = examService.createExam(request);
            
            assertEquals((long) (i + 1), response.getFieldId());
            assertEquals((long) (i + 10), response.getTopicId());
            assertEquals((long) (i % 3 + 1), response.getLevelId());
        }
    }

    @Test
    public void testExamCreationWithZeroIds() {
        // Test edge case with ID = 0
        ExamRequest request = createExamRequest();
        request.setFieldId(0L);
        request.setTopicId(0L);
        request.setLevelId(0L);
        
        ExamResponse response = examService.createExam(request);
        
        assertNotNull(response.getId());
        assertEquals(0L, response.getFieldId());
        assertEquals(0L, response.getTopicId());
        assertEquals(0L, response.getLevelId());
    }

    @Test
    public void testExamCreationWithLargeIds() {
        // Test with very large ID values
        ExamRequest request = createExamRequest();
        request.setFieldId(Long.MAX_VALUE - 1);
        request.setTopicId(Long.MAX_VALUE - 2);
        request.setLevelId(Long.MAX_VALUE - 3);
        
        ExamResponse response = examService.createExam(request);
        
        assertNotNull(response.getId());
        assertEquals(Long.MAX_VALUE - 1, response.getFieldId());
        assertEquals(Long.MAX_VALUE - 2, response.getTopicId());
        assertEquals(Long.MAX_VALUE - 3, response.getLevelId());
    }
    
    /**
     * Property Test: For any exam creation request with invalid IDs,
     * the system should reject the request before persisting any data.
     */
    @Test
    public void testExamCreationWithInvalidFieldId() {
        // Test with non-existent field ID (assuming ID 999999 doesn't exist)
        ExamRequest request = createExamRequest();
        request.setFieldId(999999L);
        request.setTopicId(1L);
        request.setLevelId(1L);
        
        // Should throw exception for invalid field ID
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            examService.createExam(request);
        });
        
        assertTrue(exception.getMessage().contains("Field not found"),
            "Exception should mention field not found");
        
        // Verify no exam was created
        assertEquals(0, examRepository.count(), "No exam should be persisted on validation failure");
    }
    
    @Test
    public void testExamCreationWithInvalidTopicId() {
        // Test with non-existent topic ID
        ExamRequest request = createExamRequest();
        request.setFieldId(1L);
        request.setTopicId(999999L);
        request.setLevelId(1L);
        
        // Should throw exception for invalid topic ID
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            examService.createExam(request);
        });
        
        assertTrue(exception.getMessage().contains("Topic not found"),
            "Exception should mention topic not found");
        
        // Verify no exam was created
        assertEquals(0, examRepository.count(), "No exam should be persisted on validation failure");
    }
    
    @Test
    public void testExamCreationWithInvalidLevelId() {
        // Test with non-existent level ID
        ExamRequest request = createExamRequest();
        request.setFieldId(1L);
        request.setTopicId(1L);
        request.setLevelId(999999L);
        
        // Should throw exception for invalid level ID
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            examService.createExam(request);
        });
        
        assertTrue(exception.getMessage().contains("Level not found"),
            "Exception should mention level not found");
        
        // Verify no exam was created
        assertEquals(0, examRepository.count(), "No exam should be persisted on validation failure");
    }
    
    /**
     * Property Test: Run multiple iterations with random invalid IDs
     * to ensure validation is consistent.
     */
    @Test
    public void testExamCreationWithRandomInvalidIds() {
        for (int iteration = 0; iteration < 50; iteration++) {
            // Generate random invalid IDs (very large numbers unlikely to exist)
            Long invalidId = 900000L + random.nextInt(100000);
            
            ExamRequest request = createExamRequest();
            
            // Randomly pick which ID to make invalid
            int invalidField = random.nextInt(3);
            switch (invalidField) {
                case 0:
                    request.setFieldId(invalidId);
                    request.setTopicId(1L);
                    request.setLevelId(1L);
                    break;
                case 1:
                    request.setFieldId(1L);
                    request.setTopicId(invalidId);
                    request.setLevelId(1L);
                    break;
                case 2:
                    request.setFieldId(1L);
                    request.setTopicId(1L);
                    request.setLevelId(invalidId);
                    break;
            }
            
            // Should throw exception
            final int iter = iteration;
            assertThrows(RuntimeException.class, () -> {
                examService.createExam(request);
            }, "Iteration " + iter + ": Should reject invalid ID");
            
            // Verify no exam was created
            assertEquals(0, examRepository.count(), 
                "Iteration " + iter + ": No exam should be persisted on validation failure");
        }
    }

    private ExamRequest createExamRequest() {
        ExamRequest request = new ExamRequest();
        request.setUserId(1L);
        request.setExamType("VIRTUAL");
        request.setTitle("Test Exam " + random.nextInt(10000));
        request.setPosition("Test Position");
        request.setTopics(Arrays.asList(1L, 2L, 3L));
        request.setQuestionTypes(Arrays.asList(1L, 2L));
        request.setQuestionCount(10);
        request.setDuration(60);
        request.setLanguage("en");
        return request;
    }
}
