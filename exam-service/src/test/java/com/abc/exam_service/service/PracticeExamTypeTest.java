package com.abc.exam_service.service;

import com.abc.exam_service.dto.ExamRequest;
import com.abc.exam_service.dto.ExamResponse;
import com.abc.exam_service.dto.UserAnswerRequest;
import com.abc.exam_service.dto.UserAnswerResponse;
import com.abc.exam_service.dto.ResultRequest;
import com.abc.exam_service.dto.ResultResponse;
import com.abc.exam_service.entity.Exam;
import com.abc.exam_service.repository.ExamRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for PRACTICE exam type feature
 * 
 * Validates:
 * - PRACTICE exams are auto-published
 * - PRACTICE exams don't require registration for submissions
 * - VIRTUAL/RECRUITER exams still require registration (backward compatibility)
 */
@SpringBootTest
@Transactional
public class PracticeExamTypeTest {

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamRepository examRepository;

    @Test
    public void testPracticeExamAutoPublished() {
        // Given: A PRACTICE exam request
        ExamRequest request = new ExamRequest();
        request.setUserId(1L);
        request.setExamType("PRACTICE");
        request.setTitle("Test Practice Exam");
        request.setPosition("Developer");
        request.setFieldId(1L);
        request.setTopicIds(Arrays.asList(1L, 2L));
        request.setLevelId(1L);
        request.setQuestionTypeIds(Arrays.asList(1L));
        request.setQuestionCount(10);
        request.setDuration(60);
        request.setLanguage("en");

        // When: Creating the exam
        ExamResponse response = examService.createExam(request);

        // Then: Status should be PUBLISHED
        assertNotNull(response);
        assertEquals("PRACTICE", response.getExamType());
        assertEquals("PUBLISHED", response.getStatus(), 
            "PRACTICE exam should be auto-published");
        
        System.out.println("✓ Test passed: PRACTICE exam auto-published with status=" + response.getStatus());
    }

    @Test
    public void testVirtualExamStaysDraft() {
        // Given: A VIRTUAL exam request
        ExamRequest request = new ExamRequest();
        request.setUserId(1L);
        request.setExamType("VIRTUAL");
        request.setTitle("Test Virtual Exam");
        request.setPosition("Developer");
        request.setFieldId(1L);
        request.setTopicIds(Arrays.asList(1L, 2L));
        request.setLevelId(1L);
        request.setQuestionTypeIds(Arrays.asList(1L));
        request.setQuestionCount(10);
        request.setDuration(60);
        request.setLanguage("en");

        // When: Creating the exam
        ExamResponse response = examService.createExam(request);

        // Then: Status should be DRAFT
        assertNotNull(response);
        assertEquals("VIRTUAL", response.getExamType());
        assertEquals("DRAFT", response.getStatus(), 
            "VIRTUAL exam should remain as DRAFT (backward compatibility)");
        
        System.out.println("✓ Test passed: VIRTUAL exam stays DRAFT with status=" + response.getStatus());
    }

    @Test
    public void testPracticeExamAnswerSubmissionWithoutRegistration() {
        // Given: A PRACTICE exam
        ExamRequest examRequest = new ExamRequest();
        examRequest.setUserId(1L);
        examRequest.setExamType("PRACTICE");
        examRequest.setTitle("Test Practice Exam for Answer");
        examRequest.setPosition("Developer");
        examRequest.setFieldId(1L);
        examRequest.setTopicIds(Arrays.asList(1L));
        examRequest.setLevelId(1L);
        examRequest.setQuestionTypeIds(Arrays.asList(1L));
        examRequest.setQuestionCount(5);
        examRequest.setDuration(30);
        examRequest.setLanguage("en");

        ExamResponse exam = examService.createExam(examRequest);

        // When: Submitting an answer without registration
        UserAnswerRequest answerRequest = new UserAnswerRequest();
        answerRequest.setExamId(exam.getId());
        answerRequest.setUserId(1L);
        answerRequest.setQuestionId(1L);
        answerRequest.setAnswerContent("Test answer");

        // Then: Should succeed without registration
        UserAnswerResponse answerResponse = assertDoesNotThrow(() -> 
            examService.submitAnswer(answerRequest),
            "PRACTICE exam should allow answer submission without registration");

        assertNotNull(answerResponse);
        assertEquals(exam.getId(), answerResponse.getExamId());
        
        System.out.println("✓ Test passed: PRACTICE exam allows answer submission without registration");
    }

    @Test
    public void testPracticeExamResultSubmissionWithoutRegistration() {
        // Given: A PRACTICE exam
        ExamRequest examRequest = new ExamRequest();
        examRequest.setUserId(1L);
        examRequest.setExamType("PRACTICE");
        examRequest.setTitle("Test Practice Exam for Result");
        examRequest.setPosition("Developer");
        examRequest.setFieldId(1L);
        examRequest.setTopicIds(Arrays.asList(1L));
        examRequest.setLevelId(1L);
        examRequest.setQuestionTypeIds(Arrays.asList(1L));
        examRequest.setQuestionCount(5);
        examRequest.setDuration(30);
        examRequest.setLanguage("en");

        ExamResponse exam = examService.createExam(examRequest);

        // When: Submitting a result without registration
        ResultRequest resultRequest = new ResultRequest();
        resultRequest.setExamId(exam.getId());
        resultRequest.setUserId(1L);
        resultRequest.setScore(85.5);
        resultRequest.setPassStatus(true);
        resultRequest.setFeedback("Good job!");

        // Then: Should succeed without registration
        ResultResponse resultResponse = assertDoesNotThrow(() -> 
            examService.submitResult(resultRequest),
            "PRACTICE exam should allow result submission without registration");

        assertNotNull(resultResponse);
        assertEquals(exam.getId(), resultResponse.getExamId());
        assertEquals(85.5, resultResponse.getScore());
        
        System.out.println("✓ Test passed: PRACTICE exam allows result submission without registration");
    }

    @Test
    public void testVirtualExamRequiresRegistrationForAnswer() {
        // Given: A VIRTUAL exam
        ExamRequest examRequest = new ExamRequest();
        examRequest.setUserId(1L);
        examRequest.setExamType("VIRTUAL");
        examRequest.setTitle("Test Virtual Exam for Answer");
        examRequest.setPosition("Developer");
        examRequest.setFieldId(1L);
        examRequest.setTopicIds(Arrays.asList(1L));
        examRequest.setLevelId(1L);
        examRequest.setQuestionTypeIds(Arrays.asList(1L));
        examRequest.setQuestionCount(5);
        examRequest.setDuration(30);
        examRequest.setLanguage("en");

        ExamResponse exam = examService.createExam(examRequest);

        // When: Trying to submit an answer without registration
        UserAnswerRequest answerRequest = new UserAnswerRequest();
        answerRequest.setExamId(exam.getId());
        answerRequest.setUserId(1L);
        answerRequest.setQuestionId(1L);
        answerRequest.setAnswerContent("Test answer");

        // Then: Should throw exception about registration
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            examService.submitAnswer(answerRequest),
            "VIRTUAL exam should require registration for answer submission");

        assertTrue(exception.getMessage().contains("register") || 
                   exception.getMessage().contains("Registration"),
            "Error message should mention registration requirement");
        
        System.out.println("✓ Test passed: VIRTUAL exam requires registration (error: " + exception.getMessage() + ")");
    }

    @Test
    public void testCaseInsensitivePracticeExamType() {
        // Test that "practice", "PRACTICE", "Practice" all work
        String[] variations = {"PRACTICE", "practice", "Practice", "pRaCtIcE"};

        for (String examType : variations) {
            ExamRequest request = new ExamRequest();
            request.setUserId(1L);
            request.setExamType(examType);
            request.setTitle("Test " + examType);
            request.setPosition("Developer");
            request.setFieldId(1L);
            request.setTopicIds(Arrays.asList(1L));
            request.setLevelId(1L);
            request.setQuestionTypeIds(Arrays.asList(1L));
            request.setQuestionCount(5);
            request.setDuration(30);
            request.setLanguage("en");

            ExamResponse response = examService.createExam(request);

            assertEquals("PUBLISHED", response.getStatus(), 
                "Exam type '" + examType + "' should be auto-published");
        }
        
        System.out.println("✓ Test passed: PRACTICE exam type is case-insensitive");
    }
}
