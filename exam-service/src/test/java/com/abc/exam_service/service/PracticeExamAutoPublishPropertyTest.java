package com.abc.exam_service.service;

import com.abc.exam_service.dto.ExamRequest;
import com.abc.exam_service.dto.ExamResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: practice-exam-type, Property 1: PRACTICE exam auto-publish
 * 
 * Property-based test to verify that PRACTICE exams are automatically published
 * when created, while other exam types remain as DRAFT.
 * Validates Requirements 1.1, 5.3
 */
@SpringBootTest
@Transactional
public class PracticeExamAutoPublishPropertyTest {

    @Autowired
    private ExamService examService;

    private final Random random = new Random();

    /**
     * Property: For any exam creation request with examType="PRACTICE",
     * the created exam SHALL have status="PUBLISHED"
     */
    @Test
    public void practiceExamShouldBeAutoPublished() {
        // Run property test with 100 iterations
        for (int i = 0; i < 100; i++) {
            // Generate random PRACTICE exam request
            ExamRequest request = createRandomExamRequest("PRACTICE");
            
            // Create exam
            ExamResponse response = examService.createExam(request);
            
            // Verify status is PUBLISHED
            assertNotNull(response.getStatus(),
                    "Iteration " + i + ": Status should not be null");
            assertEquals("PUBLISHED", response.getStatus(),
                    "Iteration " + i + ": PRACTICE exam should be auto-published");
            assertEquals("PRACTICE", response.getExamType(),
                    "Iteration " + i + ": Exam type should be PRACTICE");
        }
    }

    /**
     * Property: For any exam creation request with examType="PRACTICE" (case-insensitive),
     * the created exam SHALL have status="PUBLISHED"
     */
    @Test
    public void practiceExamShouldBeAutoPublishedCaseInsensitive() {
        String[] practiceVariations = {"PRACTICE", "practice", "Practice", "pRaCtIcE"};
        
        for (int i = 0; i < 100; i++) {
            // Use random case variation
            String examType = practiceVariations[random.nextInt(practiceVariations.length)];
            ExamRequest request = createRandomExamRequest(examType);
            
            // Create exam
            ExamResponse response = examService.createExam(request);
            
            // Verify status is PUBLISHED regardless of case
            assertEquals("PUBLISHED", response.getStatus(),
                    "Iteration " + i + ": PRACTICE exam (case: " + examType + ") should be auto-published");
        }
    }

    /**
     * Property: For any exam creation request with examType="VIRTUAL" or "RECRUITER",
     * the created exam SHALL have status="DRAFT"
     */
    @Test
    public void nonPracticeExamShouldRemainDraft() {
        String[] nonPracticeTypes = {"VIRTUAL", "RECRUITER"};
        
        for (int i = 0; i < 100; i++) {
            // Alternate between VIRTUAL and RECRUITER
            String examType = nonPracticeTypes[i % 2];
            ExamRequest request = createRandomExamRequest(examType);
            
            // Create exam
            ExamResponse response = examService.createExam(request);
            
            // Verify status is DRAFT
            assertEquals("DRAFT", response.getStatus(),
                    "Iteration " + i + ": " + examType + " exam should remain DRAFT");
            assertEquals(examType, response.getExamType(),
                    "Iteration " + i + ": Exam type should be " + examType);
        }
    }

    private ExamRequest createRandomExamRequest(String examType) {
        ExamRequest request = new ExamRequest();
        request.setUserId((long) random.nextInt(1, 1000));
        request.setExamType(examType);
        request.setTitle("Test Exam " + random.nextInt(10000));
        request.setPosition("Position " + random.nextInt(100));
        request.setFieldId((long) random.nextInt(1, 10));
        request.setTopicIds(List.of((long) random.nextInt(1, 20)));
        request.setLevelId((long) random.nextInt(1, 5));
        request.setQuestionTypeIds(List.of((long) random.nextInt(1, 5)));
        request.setQuestionCount(random.nextInt(5, 50));
        request.setDuration(random.nextInt(30, 180));
        request.setLanguage("en");
        return request;
    }
}
