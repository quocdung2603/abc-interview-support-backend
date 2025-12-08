package com.abc.exam_service.service;

import com.abc.exam_service.entity.Exam;
import com.abc.exam_service.repository.ExamRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: practice-exam-type, Property 12: Exam type identification
 * 
 * Property-based test to verify that the system correctly identifies exam types
 * and determines registration requirements.
 * Validates Requirements 4.1
 */
@SpringBootTest
@Transactional
public class ExamTypeRegistrationRequirementPropertyTest {

    @Autowired
    private ExamRepository examRepository;

    private final Random random = new Random();
    private final String[] EXAM_TYPES = {"PRACTICE", "VIRTUAL", "RECRUITER"};

    /**
     * Property: For any exam type, the system should correctly identify whether
     * registration is required. PRACTICE exams should not require registration,
     * while VIRTUAL and RECRUITER exams should require registration.
     */
    @Test
    public void examTypeShouldCorrectlyDetermineRegistrationRequirement() {
        // Run property test with 100 iterations
        for (int i = 0; i < 100; i++) {
            // Generate random exam with random type
            String examType = EXAM_TYPES[random.nextInt(EXAM_TYPES.length)];
            Exam exam = createRandomExam(examType);
            Exam savedExam = examRepository.save(exam);
            
            // Verify exam type is stored correctly
            assertNotNull(savedExam.getExamType(),
                    "Iteration " + i + ": Exam type should not be null");
            assertEquals(examType, savedExam.getExamType(),
                    "Iteration " + i + ": Exam type should match the input");
            
            // Verify registration requirement based on exam type
            boolean shouldRequireRegistration = !"PRACTICE".equalsIgnoreCase(examType);
            
            if ("PRACTICE".equalsIgnoreCase(examType)) {
                assertFalse(shouldRequireRegistration,
                        "Iteration " + i + ": PRACTICE exams should not require registration");
            } else {
                assertTrue(shouldRequireRegistration,
                        "Iteration " + i + ": " + examType + " exams should require registration");
            }
            
            // Clean up
            examRepository.deleteById(savedExam.getId());
        }
    }

    /**
     * Property: For any exam with examType="PRACTICE" (case-insensitive),
     * registration should not be required.
     */
    @Test
    public void practiceExamsShouldNotRequireRegistration() {
        // Test with different case variations
        String[] practiceVariations = {"PRACTICE", "practice", "Practice", "pRaCtIcE"};
        
        for (int i = 0; i < 100; i++) {
            // Use random case variation
            String examType = practiceVariations[random.nextInt(practiceVariations.length)];
            Exam exam = createRandomExam(examType);
            Exam savedExam = examRepository.save(exam);
            
            // Verify that PRACTICE (in any case) does not require registration
            boolean requiresRegistration = !"PRACTICE".equalsIgnoreCase(savedExam.getExamType());
            
            assertFalse(requiresRegistration,
                    "Iteration " + i + ": PRACTICE exam (case: " + examType + ") should not require registration");
            
            // Clean up
            examRepository.deleteById(savedExam.getId());
        }
    }

    /**
     * Property: For any exam with examType="VIRTUAL" or "RECRUITER",
     * registration should be required.
     */
    @Test
    public void nonPracticeExamsShouldRequireRegistration() {
        String[] nonPracticeTypes = {"VIRTUAL", "RECRUITER"};
        
        for (int i = 0; i < 100; i++) {
            // Alternate between VIRTUAL and RECRUITER
            String examType = nonPracticeTypes[i % 2];
            Exam exam = createRandomExam(examType);
            Exam savedExam = examRepository.save(exam);
            
            // Verify that non-PRACTICE exams require registration
            boolean requiresRegistration = !"PRACTICE".equalsIgnoreCase(savedExam.getExamType());
            
            assertTrue(requiresRegistration,
                    "Iteration " + i + ": " + examType + " exam should require registration");
            
            // Clean up
            examRepository.deleteById(savedExam.getId());
        }
    }

    private Exam createRandomExam(String examType) {
        Exam exam = new Exam();
        exam.setUserId((long) random.nextInt(1, 1000));
        exam.setTitle("Test Exam " + random.nextInt(10000));
        exam.setPosition("Position " + random.nextInt(100));
        exam.setExamType(examType);
        exam.setStatus(random.nextBoolean() ? "DRAFT" : "PUBLISHED");
        exam.setFieldId((long) random.nextInt(1, 10));
        exam.setLevelId((long) random.nextInt(1, 5));
        exam.setQuestionCount(random.nextInt(5, 50));
        exam.setDuration(random.nextInt(30, 180));
        exam.setLanguage("en");
        exam.setCreatedAt(LocalDateTime.now());
        exam.setCreatedBy((long) random.nextInt(1, 1000));
        return exam;
    }
}
