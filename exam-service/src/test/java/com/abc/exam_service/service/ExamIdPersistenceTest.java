package com.abc.exam_service.service;

import com.abc.exam_service.dto.ExamRequest;
import com.abc.exam_service.dto.ExamResponse;
import com.abc.exam_service.entity.Exam;
import com.abc.exam_service.repository.ExamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: exam-question-api-fixes, Property 5: Numeric IDs are persisted correctly
 * 
 * Tests for numeric ID persistence in database.
 * Validates Requirements 3.3, 4.3
 */
@SpringBootTest
@ActiveProfiles("test")
public class ExamIdPersistenceTest {

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamRepository examRepository;

    private Random random = new Random();

    @BeforeEach
    public void setup() {
        examRepository.deleteAll();
    }

    /**
     * Property Test: For any exam created with fieldId, topicId, and levelId,
     * querying the database should return those exact numeric values without
     * text conversion or encoding issues.
     */
    @Test
    public void testNumericIdPersistence() {
        // Run property test with 100 iterations
        for (int iteration = 0; iteration < 100; iteration++) {
            // Generate random IDs
            Long fieldId = (long) (1 + random.nextInt(1000));
            Long topicId = (long) (1 + random.nextInt(1000));
            Long levelId = (long) (1 + random.nextInt(100));
            
            // Create exam with numeric IDs
            ExamRequest request = createExamRequest();
            request.setFieldId(fieldId);
            request.setTopicId(topicId);
            request.setLevelId(levelId);
            
            ExamResponse response = examService.createExam(request);
            Long examId = response.getId();
            
            // Retrieve from database
            Optional<Exam> examOpt = examRepository.findById(examId);
            assertTrue(examOpt.isPresent(), 
                "Iteration " + iteration + ": Exam should exist in database");
            
            Exam exam = examOpt.get();
            
            // Verify exact numeric values are persisted
            assertEquals(fieldId, exam.getFieldId(),
                "Iteration " + iteration + ": Field ID should be persisted as exact numeric value");
            assertEquals(topicId, exam.getTopicId(),
                "Iteration " + iteration + ": Topic ID should be persisted as exact numeric value");
            assertEquals(levelId, exam.getLevelId(),
                "Iteration " + iteration + ": Level ID should be persisted as exact numeric value");
            
            // Verify no text conversion occurred
            assertNotNull(exam.getFieldId(), 
                "Iteration " + iteration + ": Field ID should not be null");
            assertNotNull(exam.getTopicId(),
                "Iteration " + iteration + ": Topic ID should not be null");
            assertNotNull(exam.getLevelId(),
                "Iteration " + iteration + ": Level ID should not be null");
            
            // Clean up
            examRepository.deleteById(examId);
        }
    }

    @Test
    public void testIdPersistenceWithNullValues() {
        // Test that null IDs are persisted as null
        ExamRequest request = createExamRequest();
        request.setFieldId(null);
        request.setTopicId(null);
        request.setLevelId(null);
        
        ExamResponse response = examService.createExam(request);
        
        Optional<Exam> examOpt = examRepository.findById(response.getId());
        assertTrue(examOpt.isPresent());
        
        Exam exam = examOpt.get();
        assertNull(exam.getFieldId());
        assertNull(exam.getTopicId());
        assertNull(exam.getLevelId());
    }

    @Test
    public void testIdPersistenceAfterUpdate() {
        // Create exam with initial IDs
        ExamRequest request = createExamRequest();
        request.setFieldId(1L);
        request.setTopicId(2L);
        request.setLevelId(3L);
        
        ExamResponse response = examService.createExam(request);
        Long examId = response.getId();
        
        // Update exam with new IDs
        ExamRequest updateRequest = createExamRequest();
        updateRequest.setFieldId(10L);
        updateRequest.setTopicId(20L);
        updateRequest.setLevelId(30L);
        
        examService.updateExam(examId, updateRequest);
        
        // Verify updated IDs are persisted
        Optional<Exam> examOpt = examRepository.findById(examId);
        assertTrue(examOpt.isPresent());
        
        Exam exam = examOpt.get();
        assertEquals(10L, exam.getFieldId());
        assertEquals(20L, exam.getTopicId());
        assertEquals(30L, exam.getLevelId());
    }

    @Test
    public void testIdPersistenceWithExtremeValues() {
        // Test with minimum and maximum Long values
        ExamRequest request1 = createExamRequest();
        request1.setFieldId(Long.MIN_VALUE);
        request1.setTopicId(0L);
        request1.setLevelId(Long.MAX_VALUE);
        
        ExamResponse response1 = examService.createExam(request1);
        
        Optional<Exam> exam1Opt = examRepository.findById(response1.getId());
        assertTrue(exam1Opt.isPresent());
        
        Exam exam1 = exam1Opt.get();
        assertEquals(Long.MIN_VALUE, exam1.getFieldId());
        assertEquals(0L, exam1.getTopicId());
        assertEquals(Long.MAX_VALUE, exam1.getLevelId());
    }

    @Test
    public void testMultipleExamsWithSameIds() {
        // Test that multiple exams can have the same field/topic/level IDs
        Long sharedFieldId = 100L;
        Long sharedTopicId = 200L;
        Long sharedLevelId = 5L;
        
        for (int i = 0; i < 10; i++) {
            ExamRequest request = createExamRequest();
            request.setFieldId(sharedFieldId);
            request.setTopicId(sharedTopicId);
            request.setLevelId(sharedLevelId);
            
            ExamResponse response = examService.createExam(request);
            
            Optional<Exam> examOpt = examRepository.findById(response.getId());
            assertTrue(examOpt.isPresent());
            
            Exam exam = examOpt.get();
            assertEquals(sharedFieldId, exam.getFieldId());
            assertEquals(sharedTopicId, exam.getTopicId());
            assertEquals(sharedLevelId, exam.getLevelId());
        }
    }

    @Test
    public void testIdPersistenceConsistency() {
        // Test that IDs remain consistent across multiple reads
        ExamRequest request = createExamRequest();
        request.setFieldId(42L);
        request.setTopicId(84L);
        request.setLevelId(7L);
        
        ExamResponse response = examService.createExam(request);
        Long examId = response.getId();
        
        // Read multiple times
        for (int i = 0; i < 10; i++) {
            Optional<Exam> examOpt = examRepository.findById(examId);
            assertTrue(examOpt.isPresent());
            
            Exam exam = examOpt.get();
            assertEquals(42L, exam.getFieldId(), "Read " + i + ": Field ID should be consistent");
            assertEquals(84L, exam.getTopicId(), "Read " + i + ": Topic ID should be consistent");
            assertEquals(7L, exam.getLevelId(), "Read " + i + ": Level ID should be consistent");
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
