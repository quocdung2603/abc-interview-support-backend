package com.abc.exam_service.registration;

import com.abc.exam_service.dto.ExamRegistrationRequest;
import com.abc.exam_service.dto.ExamRegistrationResponse;
import com.abc.exam_service.entity.Exam;
import com.abc.exam_service.repository.ExamRegistrationRepository;
import com.abc.exam_service.repository.ExamRepository;
import com.abc.exam_service.service.ExamService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: exam-registration-post-improvements, Property 3: Registration creates bidirectional relationship
 * 
 * Property-based test to verify that creating a registration establishes bidirectional relationship.
 * Validates Requirements 2.1, 2.3
 */
@SpringBootTest
@Transactional
public class ExamRegistrationBidirectionalPropertyTest {

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamRegistrationRepository examRegistrationRepository;

    private final Random random = new Random();

    /**
     * Property: For any registration created for an exam, querying registrations by that exam ID
     * should include the newly created registration.
     */
    @Test
    public void registrationShouldAppearInExamRegistrationList() {
        // Run property test with 100 iterations
        for (int i = 0; i < 100; i++) {
            // Generate random exam
            Exam exam = createRandomExam();
            Exam savedExam = examRepository.save(exam);
            
            // Generate random user ID
            Long userId = (long) random.nextInt(1, 10000);
            
            // Verify no registrations exist initially
            Page<ExamRegistrationResponse> initialRegistrations = 
                    examService.listRegistrationsByExam(savedExam.getId(), PageRequest.of(0, 10));
            
            int initialCount = initialRegistrations.getContent().size();
            
            // Create registration request
            ExamRegistrationRequest request = new ExamRegistrationRequest();
            request.setExamId(savedExam.getId());
            request.setUserId(userId);
            
            // Register for exam
            ExamRegistrationResponse registrationResponse = examService.registerForExam(request);
            
            // Query registrations by exam ID
            Page<ExamRegistrationResponse> examRegistrations = 
                    examService.listRegistrationsByExam(savedExam.getId(), PageRequest.of(0, 10));
            
            // Verify the registration appears in the exam's registration list
            assertEquals(initialCount + 1, examRegistrations.getContent().size(),
                    "Iteration " + i + ": Registration count should increase by 1");
            
            boolean found = examRegistrations.getContent().stream()
                    .anyMatch(reg -> reg.getId().equals(registrationResponse.getId()));
            
            assertTrue(found,
                    "Iteration " + i + ": Newly created registration should appear in exam's registration list");
            
            // Verify the registration has correct exam ID
            ExamRegistrationResponse foundRegistration = examRegistrations.getContent().stream()
                    .filter(reg -> reg.getId().equals(registrationResponse.getId()))
                    .findFirst()
                    .orElseThrow();
            
            assertEquals(savedExam.getId(), foundRegistration.getExamId(),
                    "Iteration " + i + ": Registration should have correct exam ID");
            assertEquals(userId, foundRegistration.getUserId(),
                    "Iteration " + i + ": Registration should have correct user ID");
            
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
