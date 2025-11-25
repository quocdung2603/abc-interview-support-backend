package com.abc.question_service.service;

import com.abc.question_service.entity.*;
import com.abc.question_service.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: exam-question-api-fixes, Property 1: Cascade delete maintains referential integrity
 * 
 * Tests for question deletion with cascade to answers.
 * Validates Requirements 1.1, 1.2
 */
@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration"
})
@ActiveProfiles("test")
@Transactional
public class QuestionDeletionTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private LevelRepository levelRepository;

    @Autowired
    private QuestionTypeRepository questionTypeRepository;

    @Autowired
    private QuestionService questionService;

    private Field testField;
    private Topic testTopic;
    private Level testLevel;
    private QuestionType testQuestionType;
    private Random random = new Random();

    @BeforeEach
    public void setup() {
        // Create test data
        testField = new Field();
        testField.setName("Test Field");
        testField.setDescription("Test Description");
        testField = fieldRepository.save(testField);

        testTopic = new Topic();
        testTopic.setName("Test Topic");
        testTopic.setDescription("Test Description");
        testTopic.setField(testField);
        testTopic = topicRepository.save(testTopic);

        testLevel = new Level();
        testLevel.setName("Test Level");
        testLevel.setDescription("Test Description");
        testLevel.setMinScore(0);
        testLevel.setMaxScore(100);
        testLevel = levelRepository.save(testLevel);

        testQuestionType = new QuestionType();
        testQuestionType.setName("Test Type");
        testQuestionType.setDescription("Test Description");
        testQuestionType = questionTypeRepository.save(testQuestionType);
    }

    /**
     * Property Test: For any question with associated answers, when the question is deleted,
     * all associated answers should also be deleted and no orphaned answer records should remain.
     */
    @Test
    public void testCascadeDeleteWithMultipleAnswers() {
        // Run property test with 100 iterations
        for (int iteration = 0; iteration < 100; iteration++) {
            // Generate random number of answers (0-10)
            int answerCount = random.nextInt(11);
            
            // Create question
            Question question = createTestQuestion();
            question = questionRepository.save(question);
            
            // Create random number of answers
            for (int i = 0; i < answerCount; i++) {
                Answer answer = createTestAnswer(question);
                answerRepository.save(answer);
            }
            
            Long questionId = question.getId();
            
            // Verify answers exist
            long answersBeforeDelete = answerRepository.countByQuestionId(questionId);
            assertEquals(answerCount, answersBeforeDelete, 
                "Iteration " + iteration + ": Expected " + answerCount + " answers before delete");
            
            // Delete question
            questionService.deleteQuestion(questionId);
            
            // Verify question is deleted
            assertFalse(questionRepository.existsById(questionId),
                "Iteration " + iteration + ": Question should be deleted");
            
            // Verify all answers are deleted (no orphaned records)
            long answersAfterDelete = answerRepository.countByQuestionId(questionId);
            assertEquals(0, answersAfterDelete,
                "Iteration " + iteration + ": All answers should be deleted, but found " + answersAfterDelete);
        }
    }

    @Test
    public void testDeleteQuestionWithNoAnswers() {
        // Create question without answers
        Question question = createTestQuestion();
        question = questionRepository.save(question);
        Long questionId = question.getId();

        // Verify no answers exist
        long answerCount = answerRepository.countByQuestionId(questionId);
        assertEquals(0, answerCount);

        // Delete question
        questionService.deleteQuestion(questionId);

        // Verify question is deleted
        assertFalse(questionRepository.existsById(questionId));
    }

    @Test
    public void testDeleteQuestionWithSingleAnswer() {
        // Create question with one answer
        Question question = createTestQuestion();
        question = questionRepository.save(question);
        
        Answer answer = createTestAnswer(question);
        answerRepository.save(answer);
        
        Long questionId = question.getId();

        // Verify answer exists
        assertEquals(1, answerRepository.countByQuestionId(questionId));

        // Delete question
        questionService.deleteQuestion(questionId);

        // Verify both question and answer are deleted
        assertFalse(questionRepository.existsById(questionId));
        assertEquals(0, answerRepository.countByQuestionId(questionId));
    }

    @Test
    public void testDeleteNonExistentQuestion() {
        Long nonExistentId = 999999L;
        
        // Attempt to delete non-existent question
        Exception exception = assertThrows(RuntimeException.class, () -> {
            questionService.deleteQuestion(nonExistentId);
        });
        
        assertTrue(exception.getMessage().contains("Question not found"));
    }

    @Test
    public void testMultipleQuestionsWithAnswers() {
        // Create multiple questions with answers
        Question q1 = createTestQuestion();
        q1 = questionRepository.save(q1);
        Answer a1 = createTestAnswer(q1);
        answerRepository.save(a1);

        Question q2 = createTestQuestion();
        q2 = questionRepository.save(q2);
        Answer a2 = createTestAnswer(q2);
        answerRepository.save(a2);

        Long q1Id = q1.getId();
        Long q2Id = q2.getId();

        // Delete first question
        questionService.deleteQuestion(q1Id);

        // Verify only first question and its answer are deleted
        assertFalse(questionRepository.existsById(q1Id));
        assertEquals(0, answerRepository.countByQuestionId(q1Id));
        
        // Verify second question and its answer still exist
        assertTrue(questionRepository.existsById(q2Id));
        assertEquals(1, answerRepository.countByQuestionId(q2Id));
    }

    private Question createTestQuestion() {
        Question question = new Question();
        question.setUserId(1L);
        question.setQuestionContent("Test Question " + random.nextInt(10000));
        question.setQuestionAnswer("Test Answer");
        question.setStatus("PENDING");
        question.setLanguage("en");
        question.setCreatedAt(LocalDateTime.now());
        question.setUsefulVote(0);
        question.setUnusefulVote(0);
        question.setTopic(testTopic);
        question.setField(testField);
        question.setLevel(testLevel);
        question.setQuestionType(testQuestionType);
        return question;
    }

    private Answer createTestAnswer(Question question) {
        Answer answer = new Answer();
        answer.setUserId(1L);
        answer.setQuestion(question);
        answer.setQuestionType(testQuestionType);
        answer.setContent("Test Answer Content " + random.nextInt(10000));
        answer.setIsCorrect(random.nextBoolean());
        answer.setIsSampleAnswer(false);
        answer.setOrderNumber(random.nextInt(10));
        answer.setUsefulVote(0);
        answer.setUnusefulVote(0);
        answer.setCreatedAt(LocalDateTime.now());
        return answer;
    }
}
