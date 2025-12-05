package com.abc.exam_service.registration;

import com.abc.exam_service.dto.ExamRegistrationRequest;
import com.abc.exam_service.dto.ExamRegistrationResponse;
import com.abc.exam_service.entity.Exam;
import com.abc.exam_service.entity.ExamRegistration;
import com.abc.exam_service.mapper.Mappers;
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
 * Feature: exam-registration-post-improvements, Property 1: Registration responses include examId
 * 
 * Property-based test to verify that ExamRegistrationResponse contains examId field.
 * Validates Requirements 1.1, 1.2, 1.4
 */
@SpringBootTest
@Transactional
public class ExamRegistrationExamIdPropertyTest {

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamRegistrationRepository examRegistrationRepository;

    @Autowired
    private Mappers mappers;

    private final Random random = new Random();

    /**
     * Property: For any exam registration retrieved from the system (whether by user ID or exam ID),
     * the response DTO should contain a non-null examId field that matches the associated exam's ID.
     */
    @Test
    public void registrationResponseShouldIncludeExamId() {
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
            
            // Verify examId is present and matches
            assertNotNull(registrationResponse.getExamId(),
                    "Iteration " + i + ": examId should not be null in registration response");
            assertEquals(savedExam.getId(), registrationResponse.getExamId(),
                    "Iteration " + i + ": examId should match the exam's ID");
            
            // Test retrieval by user ID
            Page<ExamRegistrationResponse> userRegistrations = 
                    examService.listRegistrationsByUser(userId, PageRequest.of(0, 10));
            
            assertTrue(userRegistrations.getContent().size() > 0,
                    "Iteration " + i + ": should find registrations for user");
            
            for (ExamRegistrationResponse response : userRegistrations.getContent()) {
                assertNotNull(response.getExamId(),
                        "Iteration " + i + ": examId should not be null when retrieved by user ID");
                assertEquals(savedExam.getId(), response.getExamId(),
                        "Iteration " + i + ": examId should match when retrieved by user ID");
            }
            
            // Test retrieval by exam ID
            Page<ExamRegistrationResponse> examRegistrations = 
                    examService.listRegistrationsByExam(savedExam.getId(), PageRequest.of(0, 10));
            
            assertTrue(examRegistrations.getContent().size() > 0,
                    "Iteration " + i + ": should find registrations for exam");
            
            for (ExamRegistrationResponse response : examRegistrations.getContent()) {
                assertNotNull(response.getExamId(),
                        "Iteration " + i + ": examId should not be null when retrieved by exam ID");
                assertEquals(savedExam.getId(), response.getExamId(),
                        "Iteration " + i + ": examId should match when retrieved by exam ID");
            }
            
            // Test retrieval by registration ID
            ExamRegistrationResponse singleRegistration = 
                    examService.getRegistrationById(registrationResponse.getId());
            
            assertNotNull(singleRegistration.getExamId(),
                    "Iteration " + i + ": examId should not be null when retrieved by registration ID");
            assertEquals(savedExam.getId(), singleRegistration.getExamId(),
                    "Iteration " + i + ": examId should match when retrieved by registration ID");
            
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
