package com.abc.question_service.service;

import com.abc.question_service.entity.Field;
import com.abc.question_service.entity.Level;
import com.abc.question_service.entity.QuestionType;
import com.abc.question_service.entity.Topic;
import com.abc.question_service.model.CombinationKey;
import org.junit.jupiter.api.RepeatedTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for DistributionStrategy
 * 
 * **Feature: question-bulk-generation, Property 6: Minimum distribution coverage**
 * For any generation run with sufficient target count, each valid combination of 
 * field, topic, level, and questionType should have at least 10 unique questions.
 * **Validates: Requirements 2.1**
 */
public class DistributionStrategyPropertyTest {

    private final DistributionStrategy distributionStrategy = new DistributionStrategy();
    private final Random random = new Random();
    private static final int MINIMUM_QUESTIONS_PER_COMBINATION = 10;

    /**
     * Property: For any target count >= minimum required, each combination should have at least 10 questions
     */
    @RepeatedTest(100)
    public void testMinimumDistributionCoverage() {
        // Generate random test data
        TestData testData = generateTestData();
        
        // Calculate minimum required
        int totalCombinations = calculateTotalValidCombinations(
            testData.fields, testData.topics, testData.levels, testData.questionTypes
        );
        
        if (totalCombinations == 0) {
            return; // Skip if no valid combinations
        }
        
        int minimumRequired = totalCombinations * MINIMUM_QUESTIONS_PER_COMBINATION;
        
        // Use a target count that is at least the minimum required
        int targetCount = minimumRequired + random.nextInt(1000);
        
        // Calculate distribution
        Map<CombinationKey, Integer> distribution = distributionStrategy.calculateDistribution(
            targetCount,
            testData.fields,
            testData.topics,
            testData.levels,
            testData.questionTypes
        );
        
        // Verify each combination has at least minimum questions
        for (Map.Entry<CombinationKey, Integer> entry : distribution.entrySet()) {
            assertTrue(
                entry.getValue() >= MINIMUM_QUESTIONS_PER_COMBINATION,
                String.format("Combination %s should have at least %d questions, but has %d",
                    entry.getKey(), MINIMUM_QUESTIONS_PER_COMBINATION, entry.getValue())
            );
        }
        
        // Verify total count matches target
        int totalGenerated = distribution.values().stream().mapToInt(Integer::intValue).sum();
        assertEquals(targetCount, totalGenerated, "Total generated should match target count");
    }

    /**
     * Property: Distribution should only include valid combinations (topic belongs to field)
     */
    @RepeatedTest(100)
    public void testOnlyValidCombinations() {
        TestData testData = generateTestData();
        
        Map<CombinationKey, Integer> distribution = distributionStrategy.calculateDistribution(
            testData.targetCount,
            testData.fields,
            testData.topics,
            testData.levels,
            testData.questionTypes
        );
        
        // Verify each combination has valid topic-field relationship
        for (CombinationKey key : distribution.keySet()) {
            Topic matchingTopic = testData.topics.stream()
                .filter(t -> t.getId().equals(key.getTopicId()))
                .findFirst()
                .orElse(null);
            
            assertNotNull(matchingTopic, "Topic should exist");
            assertEquals(key.getFieldId(), matchingTopic.getField().getId(),
                "Topic should belong to the specified field");
        }
    }

    /**
     * Property: Total distributed count should equal target count
     */
    @RepeatedTest(100)
    public void testTotalCountMatchesTarget() {
        TestData testData = generateTestData();
        
        if (testData.targetCount <= 0) {
            return; // Skip invalid target counts
        }
        
        Map<CombinationKey, Integer> distribution = distributionStrategy.calculateDistribution(
            testData.targetCount,
            testData.fields,
            testData.topics,
            testData.levels,
            testData.questionTypes
        );
        
        int totalDistributed = distribution.values().stream().mapToInt(Integer::intValue).sum();
        
        assertEquals(testData.targetCount, totalDistributed,
            "Total distributed should match target count");
    }

    private TestData generateTestData() {
        TestData data = new TestData();
        
        // Generate 1-3 fields
        int fieldCount = 1 + random.nextInt(3);
        data.fields = new ArrayList<>();
        for (int i = 0; i < fieldCount; i++) {
            Field field = new Field();
            field.setId((long) (i + 1));
            field.setName("Field" + (i + 1));
            data.fields.add(field);
        }
        
        // Generate 2-5 topics per field
        data.topics = new ArrayList<>();
        long topicId = 1;
        for (Field field : data.fields) {
            int topicCount = 2 + random.nextInt(4);
            for (int i = 0; i < topicCount; i++) {
                Topic topic = new Topic();
                topic.setId(topicId++);
                topic.setName("Topic" + topic.getId());
                topic.setField(field);
                data.topics.add(topic);
            }
        }
        
        // Generate 2-4 levels
        int levelCount = 2 + random.nextInt(3);
        data.levels = new ArrayList<>();
        for (int i = 0; i < levelCount; i++) {
            Level level = new Level();
            level.setId((long) (i + 1));
            level.setName("Level" + (i + 1));
            data.levels.add(level);
        }
        
        // Generate 2-3 question types
        int typeCount = 2 + random.nextInt(2);
        data.questionTypes = new ArrayList<>();
        for (int i = 0; i < typeCount; i++) {
            QuestionType type = new QuestionType();
            type.setId((long) (i + 1));
            type.setName("Type" + (i + 1));
            data.questionTypes.add(type);
        }
        
        // Calculate minimum and generate target count
        int totalCombinations = calculateTotalValidCombinations(
            data.fields, data.topics, data.levels, data.questionTypes
        );
        int minimum = totalCombinations * 10;
        
        // Generate target count between minimum and minimum + 5000
        data.targetCount = minimum + random.nextInt(5001);
        
        return data;
    }

    private int calculateTotalValidCombinations(
            List<Field> fields,
            List<Topic> topics,
            List<Level> levels,
            List<QuestionType> questionTypes) {
        
        int count = 0;
        for (Field field : fields) {
            long topicsInField = topics.stream()
                .filter(t -> t.getField().getId().equals(field.getId()))
                .count();
            count += topicsInField * levels.size() * questionTypes.size();
        }
        return count;
    }

    /**
     * Test data container
     */
    private static class TestData {
        int targetCount;
        List<Field> fields;
        List<Topic> topics;
        List<Level> levels;
        List<QuestionType> questionTypes;
    }
}
