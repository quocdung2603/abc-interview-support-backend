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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: exam-registration-post-improvements, Property 4: Exam query returns all registrations
 * 
 * Property-based test to verify that querying registrations by exam ID returns all associated registrations.
 * Validates Requirements 2.2
 */
@SpringBootTest
@Transactional
public class ExamRegistrationQueryCompletenessPropertyTest {

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamRegistrationRepository examRegistrationRepository;

    private final Random random = new Random();

    /**
     * Property: For any exam with multiple registrations, querying registrations by exam ID
     * should return exactly the set of registrations associated with that exam (no more, no less).
     */
    @Test
    public void examQueryShouldReturnAllRegistrations() {
        // Run property test with 50 iterations (fewer because we create multiple registrations per iteration)
        for (int i = 0; i < 50; i++) {
            // Generate random exam
            Exam exam = createRandomExam();
            Exam savedExam = examRepository.save(exam);
            
            // Generate random number of registrations (between 1 and 10)
            int registrationCount = random.nextInt(1, 11);
            List<Long> createdRegistrationIds = new ArrayList<>();
            Set<Long> userIds = new HashSet<>();
            
            // Create multiple registrations for the exam
            for (int j = 0; j < registrationCount; j++) {
                // Generate unique user ID
                Long userId;
                do {
                    userId = (long) random.nextInt(1, 100000);
                } while (userIds.contains(userId));
                userIds.add(userId);
                
                ExamRegistrationRequest request = new ExamRegistrationRequest();
                request.setExamId(savedExam.getId());
                request.setUserId(userId);
                
                ExamRegistrationResponse response = examService.registerForExam(request);
                createdRegistrationIds.add(response.getId());
            }
            
            // Query all registrations for the exam
            Page<ExamRegistrationResponse> examRegistrations = 
                    examService.listRegistrationsByExam(savedExam.getId(), PageRequest.of(0, 100));
            
            // Verify count matches
            assertEquals(registrationCount, examRegistrations.getContent().size(),
                    "Iteration " + i + ": Should return exactly " + registrationCount + " registrations");
            
            // Verify all created registrations are returned
            Set<Long> returnedIds = new HashSet<>();
            for (ExamRegistrationResponse response : examRegistrations.getContent()) {
                returnedIds.add(response.getId());
                
                // Verify each registration has correct exam ID
                assertEquals(savedExam.getId(), response.getExamId(),
                        "Iteration " + i + ": All registrations should have correct exam ID");
            }
            
            // Verify no registrations are missing
            for (Long createdId : createdRegistrationIds) {
                assertTrue(returnedIds.contains(createdId),
                        "Iteration " + i + ": Registration " + createdId + " should be in results");
            }
            
            // Verify no extra registrations are returned
            assertEquals(createdRegistrationIds.size(), returnedIds.size(),
                    "Iteration " + i + ": Should not return extra registrations");
            
            // Create another exam to verify isolation
            Exam otherExam = createRandomExam();
            Exam savedOtherExam = examRepository.save(otherExam);
            
            // Create registration for other exam
            ExamRegistrationRequest otherRequest = new ExamRegistrationRequest();
            otherRequest.setExamId(savedOtherExam.getId());
            otherRequest.setUserId((long) random.nextInt(1, 100000));
            examService.registerForExam(otherRequest);
            
            // Query original exam again
            Page<ExamRegistrationResponse> originalExamRegistrations = 
                    examService.listRegistrationsByExam(savedExam.getId(), PageRequest.of(0, 100));
            
            // Verify count hasn't changed (other exam's registration not included)
            assertEquals(registrationCount, originalExamRegistrations.getContent().size(),
                    "Iteration " + i + ": Original exam should still have " + registrationCount + " registrations");
            
            // Clean up for next iteration
            examRegistrationRepository.deleteByExamId(savedExam.getId());
            examRegistrationRepository.deleteByExamId(savedOtherExam.getId());
            examRepository.deleteById(savedExam.getId());
            examRepository.deleteById(savedOtherExam.getId());
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
