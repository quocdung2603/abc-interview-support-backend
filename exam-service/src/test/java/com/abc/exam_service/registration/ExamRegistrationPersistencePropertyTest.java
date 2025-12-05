package com.abc.exam_service.registration;

import com.abc.exam_service.dto.ExamRegistrationRequest;
import com.abc.exam_service.dto.ExamRegistrationResponse;
import com.abc.exam_service.entity.Exam;
import com.abc.exam_service.entity.ExamRegistration;
import com.abc.exam_service.repository.ExamRegistrationRepository;
import com.abc.exam_service.repository.ExamRepository;
import com.abc.exam_service.service.ExamService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: exam-registration-post-improvements, Property 2: Registration persistence maintains exam relationship
 * 
 * Property-based test to verify that registration persistence maintains the exam relationship.
 * Validates Requirements 1.3
 */
@SpringBootTest
@Transactional
public class ExamRegistrationPersistencePropertyTest {

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamRegistrationRepository examRegistrationRepository;

    private final Random random = new Random();

    /**
     * Property: For any registration created with an exam, retrieving that registration
     * from the database should return a registration with the correct exam relationship intact.
     */
    @Test
    public void registrationPersistenceShouldMaintainExamRelationship() {
        // Run property test with 100 iterations
        for (int i = 0; i < 100; i++) {
            // Generate random exam
            Exam exam = createRandomExam();
            Exam savedExam = examRepository.save(exam);
            
            // Generate random user ID
            Long userId = (long) random.nextInt(1, 10000);
            
            // Create registration request
            ExamRegistrationRequest request = new ExamRegistrationRequest();
            request.setExamId(savedExam.getId());
            request.setUserId(userId);
            
            // Register for exam
            ExamRegistrationResponse registrationResponse = examService.registerForExam(request);
            
            // Retrieve the registration entity from database
            final int iteration = i; // Make effectively final for lambda
            ExamRegistration retrievedRegistration = examRegistrationRepository
                    .findById(registrationResponse.getId())
                    .orElseThrow(() -> new AssertionError("Iteration " + iteration + ": Registration should exist in database"));
            
            // Verify exam relationship is maintained
            assertNotNull(retrievedRegistration.getExam(),
                    "Iteration " + i + ": Exam relationship should not be null after persistence");
            
            // Force load the exam to verify relationship
            Exam retrievedExam = retrievedRegistration.getExam();
            assertNotNull(retrievedExam.getId(),
                    "Iteration " + i + ": Exam ID should not be null");
            assertEquals(savedExam.getId(), retrievedExam.getId(),
                    "Iteration " + i + ": Exam ID should match the original exam");
            
            // Verify other exam properties are accessible (relationship is properly loaded)
            assertNotNull(retrievedExam.getTitle(),
                    "Iteration " + i + ": Exam title should be accessible through relationship");
            assertEquals(savedExam.getTitle(), retrievedExam.getTitle(),
                    "Iteration " + i + ": Exam title should match");
            
            // Verify registration properties are persisted correctly
            assertEquals(userId, retrievedRegistration.getUserId(),
                    "Iteration " + i + ": User ID should be persisted correctly");
            assertEquals("REGISTERED", retrievedRegistration.getRegistrationStatus(),
                    "Iteration " + i + ": Registration status should be REGISTERED");
            assertNotNull(retrievedRegistration.getRegisteredAt(),
                    "Iteration " + i + ": Registration timestamp should be set");
            
            // Clean up for next iteration
            examRegistrationRepository.deleteByExamId(savedExam.getId());
            examRepository.deleteById(savedExam.getId());
        }
    }

    private Exam createRandomExam() {
        Exam exam = new Exam();
        exam.setUserId((long) random.nextInt(1, 1000));
        exam.setTitle("Test Exam " + random.nextInt(10000));
        exam.setPosition("Position " + random.nextInt(100));
        exam.setExamType(random.nextBoolean() ? "PRACTICE" : "VIRTUAL");
        exam.setStatus("PUBLISHED");
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
