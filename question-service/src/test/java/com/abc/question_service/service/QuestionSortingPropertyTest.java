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
 * Validates: Requirements 1.5
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class QuestionSortingPropertyTest {

    @Autowired
    private QuestionService questionService;

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
    private Long fieldId;
    private Long topicId;
    private Long levelId;
    private Long questionTypeId;

    @BeforeEach
    public void setUp() {
        questionRepository.deleteAll();
        topicRepository.deleteAll();
        fieldRepository.deleteAll();
        levelRepository.deleteAll();
        questionTypeRepository.deleteAll();

        // Create required entities
        FieldRequest fieldRequest = new FieldRequest();
        fieldRequest.setName("Test Field");
        fieldRequest.setDescription("Test Description");
        fieldId = questionService.createField(fieldRequest).getId();

        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setName("Test Topic");
        topicRequest.setDescription("Test Description");
        topicRequest.setFieldId(fieldId);
        topicId = questionService.createTopic(topicRequest).getId();

        LevelRequest levelRequest = new LevelRequest();
        levelRequest.setName("Test Level");
        levelRequest.setDescription("Test Description");
        levelRequest.setMinScore(0);
        levelRequest.setMaxScore(100);
        levelId = questionService.createLevel(levelRequest).getId();

        QuestionTypeRequest questionTypeRequest = new QuestionTypeRequest();
        questionTypeRequest.setName("Test QuestionType");
        questionTypeRequest.setDescription("Test Description");
        questionTypeId = questionService.createQuestionType(questionTypeRequest).getId();
    }

    @RepeatedTest(100)
    public void defaultSortingAppliesWhenNoSortSpecified() {
        // Create multiple questions with random data
        int numQuestions = 3 + random.nextInt(5); // 3-7 questions
        for (int i = 0; i < numQuestions; i++) {
            QuestionRequest request = new QuestionRequest();
            request.setUserId(1L);
            request.setContent("Question " + random.nextInt(1000));
            request.setAnswer("Answer " + random.nextInt(1000));
            request.setLanguage("en");
            request.setFieldId(fieldId);
            request.setTopicId(topicId);
            request.setLevelId(levelId);
            request.setQuestionTypeId(questionTypeId);
            
            questionService.createQuestion(request);
        }

        // Get all questions without explicit sort
        Pageable pageable = PageRequest.of(0, 20);
        Page<QuestionResponse> result = questionService.getAllQuestions(pageable);

        // Verify results are sorted by ID in ascending order
        List<QuestionResponse> questions = result.getContent();
        for (int i = 0; i < questions.size() - 1; i++) {
            assertTrue(questions.get(i).getId() <= questions.get(i + 1).getId(),
                    "Questions should be sorted by ID in ascending order");
        }
    }
}
