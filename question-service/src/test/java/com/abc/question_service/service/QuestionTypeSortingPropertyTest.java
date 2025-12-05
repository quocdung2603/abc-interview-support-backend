package com.abc.question_service.service;

import com.abc.question_service.dto.QuestionTypeRequest;
import com.abc.question_service.dto.QuestionTypeResponse;
import com.abc.question_service.repository.QuestionTypeRepository;
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
 * Validates: Requirements 1.4
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class QuestionTypeSortingPropertyTest {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionTypeRepository questionTypeRepository;

    private Random random = new Random();

    @BeforeEach
    public void setUp() {
        questionTypeRepository.deleteAll();
    }

    @RepeatedTest(100)
    public void defaultSortingAppliesWhenNoSortSpecified() {
        // Create multiple question types with random data
        int numQuestionTypes = 3 + random.nextInt(5); // 3-7 question types
        for (int i = 0; i < numQuestionTypes; i++) {
            QuestionTypeRequest request = new QuestionTypeRequest();
            request.setName("QuestionType " + random.nextInt(1000));
            request.setDescription("Description " + random.nextInt(1000));
            
            questionService.createQuestionType(request);
        }

        // Get all question types without explicit sort
        Pageable pageable = PageRequest.of(0, 20);
        Page<QuestionTypeResponse> result = questionService.getAllQuestionTypes(pageable);

        // Verify results are sorted by ID in ascending order
        List<QuestionTypeResponse> questionTypes = result.getContent();
        for (int i = 0; i < questionTypes.size() - 1; i++) {
            assertTrue(questionTypes.get(i).getId() <= questionTypes.get(i + 1).getId(),
                    "QuestionTypes should be sorted by ID in ascending order");
        }
    }
}
