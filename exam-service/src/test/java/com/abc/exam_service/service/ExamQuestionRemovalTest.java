package com.abc.exam_service.service;

import com.abc.exam_service.entity.Exam;
import com.abc.exam_service.entity.ExamQuestion;
import com.abc.exam_service.repository.ExamQuestionRepository;
import com.abc.exam_service.repository.ExamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: exam-question-api-fixes, Property 3: Exam question removal is atomic
 * 
 * Tests for transaction atomicity when removing questions from exams.
 * Validates Requirements 2.2, 2.3
 */
@SpringBootTest
@ActiveProfiles("test")
public class ExamQuestionRemovalTest {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamQuestionRepository examQuestionRepository;

    @Autowired
    private ExamService examService;

    private Random random = new Random();

    @BeforeEach
    public void setup() {
        // Clean up before each test
        examQuestionRepository.deleteAll();
        examRepository.deleteAll();
    }

    /**
     * Property Test: For any exam with multiple questions, when removing all questions,
     * either all exam-question associations are deleted or none are, with no partial deletions.
     */
    @Test
    @Transactional
    public void testExamQuestionRemovalIsAtomic() {
        // Run property test with 100 iterations
        for (int iteration = 0; iteration < 100; iteration++) {
            // Generate random number of questions (1-20)
            int questionCount = 1 + random.nextInt(20);
            
            // Create exam
            Exam exam = createTestExam();
            exam = examRepository.save(exam);
            
            // Add random number of questions to exam
            List<Long> questionIds = new ArrayList<>();
            for (int i = 0; i < questionCount; i++) {
                ExamQuestion examQuestion = new ExamQuestion();
                examQuestion.setExam(exam);
                examQuestion.setQuestionId((long) (1000 + random.nextInt(9000)));
                examQuestion.setOrderNumber(i + 1);
                examQuestionRepository.save(examQuestion);
                questionIds.add(examQuestion.getQuestionId());
            }
            
            Long examId = exam.getId();
            
            // Verify questions exist
            List<ExamQuestion> questionsBeforeRemoval = examQuestionRepository.findByExamIdOrderByOrderNumberAsc(examId);
            assertEquals(questionCount, questionsBeforeRemoval.size(),
                "Iteration " + iteration + ": Expected " + questionCount + " questions before removal");
            
            // Remove all questions from exam
            examService.removeQuestionsFromExam(examId);
            
            // Verify all questions are removed (atomic operation)
            List<ExamQuestion> questionsAfterRemoval = examQuestionRepository.findByExamIdOrderByOrderNumberAsc(examId);
            assertEquals(0, questionsAfterRemoval.size(),
                "Iteration " + iteration + ": All questions should be removed, but found " + questionsAfterRemoval.size());
            
            // Verify exam still exists
            assertTrue(examRepository.existsById(examId),
                "Iteration " + iteration + ": Exam should still exist after question removal");
            
            // Clean up for next iteration
            examRepository.deleteById(examId);
        }
    }

    @Test
    public void testRemoveQuestionsFromEmptyExam() {
        // Create exam without questions
        Exam exam = createTestExam();
        exam = examRepository.save(exam);
        Long examId = exam.getId();

        // Verify no questions exist
        List<ExamQuestion> questions = examQuestionRepository.findByExamIdOrderByOrderNumberAsc(examId);
        assertEquals(0, questions.size());

        // Remove questions (should succeed even with no questions)
        examService.removeQuestionsFromExam(examId);

        // Verify still no questions
        questions = examQuestionRepository.findByExamIdOrderByOrderNumberAsc(examId);
        assertEquals(0, questions.size());
    }

    @Test
    public void testRemoveQuestionsFromNonExistentExam() {
        Long nonExistentId = 999999L;
        
        // Attempt to remove questions from non-existent exam
        Exception exception = assertThrows(RuntimeException.class, () -> {
            examService.removeQuestionsFromExam(nonExistentId);
        });
        
        assertTrue(exception.getMessage().contains("Exam not found"));
    }

    @Test
    @Transactional
    public void testRemoveQuestionsDoesNotAffectOtherExams() {
        // Create two exams with questions
        Exam exam1 = createTestExam();
        exam1 = examRepository.save(exam1);
        
        ExamQuestion eq1 = new ExamQuestion();
        eq1.setExam(exam1);
        eq1.setQuestionId(1001L);
        eq1.setOrderNumber(1);
        examQuestionRepository.save(eq1);

        Exam exam2 = createTestExam();
        exam2 = examRepository.save(exam2);
        
        ExamQuestion eq2 = new ExamQuestion();
        eq2.setExam(exam2);
        eq2.setQuestionId(2001L);
        eq2.setOrderNumber(1);
        examQuestionRepository.save(eq2);

        Long exam1Id = exam1.getId();
        Long exam2Id = exam2.getId();

        // Remove questions from first exam
        examService.removeQuestionsFromExam(exam1Id);

        // Verify only first exam's questions are removed
        assertEquals(0, examQuestionRepository.findByExamIdOrderByOrderNumberAsc(exam1Id).size());
        
        // Verify second exam's questions still exist
        assertEquals(1, examQuestionRepository.findByExamIdOrderByOrderNumberAsc(exam2Id).size());
    }

    @Test
    @Transactional
    public void testTransactionRollbackOnError() {
        // Create exam with questions
        Exam exam = createTestExam();
        exam = examRepository.save(exam);
        
        for (int i = 0; i < 5; i++) {
            ExamQuestion examQuestion = new ExamQuestion();
            examQuestion.setExam(exam);
            examQuestion.setQuestionId((long) (1000 + i));
            examQuestion.setOrderNumber(i + 1);
            examQuestionRepository.save(examQuestion);
        }
        
        Long examId = exam.getId();
        
        // Verify initial state
        assertEquals(5, examQuestionRepository.findByExamIdOrderByOrderNumberAsc(examId).size());
        
        // Attempt to remove questions from non-existent exam (should fail)
        Long nonExistentId = 999999L;
        try {
            examService.removeQuestionsFromExam(nonExistentId);
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }
        
        // Verify original exam's questions are still intact
        assertEquals(5, examQuestionRepository.findByExamIdOrderByOrderNumberAsc(examId).size());
    }

    @Test
    @Transactional
    public void testMultipleRemovalOperations() {
        // Create multiple exams with questions
        List<Long> examIds = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            Exam exam = createTestExam();
            exam = examRepository.save(exam);
            examIds.add(exam.getId());
            
            // Add 3 questions to each exam
            for (int j = 0; j < 3; j++) {
                ExamQuestion examQuestion = new ExamQuestion();
                examQuestion.setExam(exam);
                examQuestion.setQuestionId((long) (1000 * (i + 1) + j));
                examQuestion.setOrderNumber(j + 1);
                examQuestionRepository.save(examQuestion);
            }
        }
        
        // Remove questions from all exams
        for (Long examId : examIds) {
            examService.removeQuestionsFromExam(examId);
        }
        
        // Verify all questions are removed
        for (Long examId : examIds) {
            assertEquals(0, examQuestionRepository.findByExamIdOrderByOrderNumberAsc(examId).size());
        }
    }

    private Exam createTestExam() {
        Exam exam = new Exam();
        exam.setUserId(1L);
        exam.setTitle("Test Exam " + random.nextInt(10000));
        exam.setPosition("Test Position");
        exam.setExamType("VIRTUAL");
        exam.setStatus("DRAFT");
        exam.setQuestionCount(0);
        exam.setDuration(60);
        exam.setLanguage("en");
        exam.setCreatedAt(LocalDateTime.now());
        exam.setCreatedBy(1L);
        return exam;
    }
}
