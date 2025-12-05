package com.abc.question_service.service;

import com.abc.question_service.dto.LevelRequest;
import com.abc.question_service.dto.LevelResponse;
import com.abc.question_service.repository.LevelRepository;
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
 * Validates: Requirements 1.3
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class LevelSortingPropertyTest {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private LevelRepository levelRepository;

    private Random random = new Random();

    @BeforeEach
    public void setUp() {
        levelRepository.deleteAll();
    }

    @RepeatedTest(100)
    public void defaultSortingAppliesWhenNoSortSpecified() {
        // Create multiple levels with random data
        int numLevels = 3 + random.nextInt(5); // 3-7 levels
        for (int i = 0; i < numLevels; i++) {
            LevelRequest request = new LevelRequest();
            request.setName("Level " + random.nextInt(1000));
            request.setDescription("Description " + random.nextInt(1000));
            request.setMinScore(random.nextInt(100));
            request.setMaxScore(random.nextInt(100) + 100);
            
            questionService.createLevel(request);
        }

        // Get all levels without explicit sort
        Pageable pageable = PageRequest.of(0, 20);
        Page<LevelResponse> result = questionService.getAllLevels(pageable);

        // Verify results are sorted by ID in ascending order
        List<LevelResponse> levels = result.getContent();
        for (int i = 0; i < levels.size() - 1; i++) {
            assertTrue(levels.get(i).getId() <= levels.get(i + 1).getId(),
                    "Levels should be sorted by ID in ascending order");
        }
    }
}
