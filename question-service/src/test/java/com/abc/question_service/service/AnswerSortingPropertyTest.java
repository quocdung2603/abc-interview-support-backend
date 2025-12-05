package com.abc.question_service.service;

import com.abc.question_service.dto.*;
import com.abc.question_service.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Feature: question-service-sorting, Property 1: Default sorting applies when no sort specified
 * Validates: Requirements 1.6
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AnswerSortingPropertyTest {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private LevelRepository levelRepository;

    @Autowired
    private QuestionTypeRepository questionTypeRepository;

    private Random random = new Random();
    private Long questionId;
    private Long questionTypeId;

    @BeforeEach
    public void setUp() {
        answerRepository.deleteAll();
        questionRepository.deleteAll();
        topicRepository.deleteAll();
        fieldRepository.deleteAll();
        levelRepository.deleteAll();
        questionTypeRepository.deleteAll();

        // Create required entities
        FieldRequest fieldRequest = new FieldRequest();
        fieldRequest.setName("Test Field");
        fieldRequest.setDescription("Test Description");
        Long fieldId = questionService.createField(fieldRequest).getId();

        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setName("Test Topic");
        topicRequest.setDescription("Test Description");
        topicRequest.setFieldId(fieldId);
        Long topicId = questionService.createTopic(topicRequest).getId();

        LevelRequest levelRequest = new LevelRequest();
        levelRequest.setName("Test Level");
        levelRequest.setDescription("Test Description");
        levelRequest.setMinScore(0);
        levelRequest.setMaxScore(100);
        Long levelId = questionService.createLevel(levelRequest).getId();

        QuestionTypeRequest questionTypeRequest = new QuestionTypeRequest();
        questionTypeRequest.setName("Test QuestionType");
        questionTypeRequest.setDescription("Test Description");
        questionTypeId = questionService.createQuestionType(questionTypeRequest).getId();

        // Create a question
        QuestionRequest questionRequest = new QuestionRequest();
        questionRequest.setUserId(1L);
        questionRequest.setContent("Test Question");
        questionRequest.setAnswer("Test Answer");
        questionRequest.setLanguage("en");
        questionRequest.setFieldId(fieldId);
        questionRequest.setTopicId(topicId);
        questionRequest.setLevelId(levelId);
        questionRequest.setQuestionTypeId(questionTypeId);
        questionId = questionService.createQuestion(questionRequest).getId();
    }

    @RepeatedTest(100)
    public void defaultSortingAppliesWhenNoSortSpecified() {
        // Create multiple answers with random data
        int numAnswers = 3 + random.nextInt(5); // 3-7 answers
        for (int i = 0; i < numAnswers; i++) {
            AnswerRequest request = new AnswerRequest();
            request.setUserId(1L);
            request.setContent("Answer " + random.nextInt(1000));
            request.setIsCorrect(random.nextBoolean());
            request.setIsSampleAnswer(random.nextBoolean());
            request.setOrderNumber(i);
            request.setQuestionId(questionId);
            request.setQuestionTypeId(questionTypeId);
            
            questionService.createAnswer(request);
        }

        // Get all answers without explicit sort
        Pageable pageable = PageRequest.of(0, 20);
        Page<AnswerResponse> result = questionService.getAllAnswers(pageable);

        // Verify results are sorted by ID in ascending order
        List<AnswerResponse> answers = result.getContent();
        for (int i = 0; i < answers.size() - 1; i++) {
            assertTrue(answers.get(i).getId() <= answers.get(i + 1).getId(),
                    "Answers should be sorted by ID in ascending order");
        }
    }
}
