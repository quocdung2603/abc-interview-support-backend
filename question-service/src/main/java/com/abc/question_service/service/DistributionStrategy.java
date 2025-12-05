package com.abc.question_service.service;

import com.abc.question_service.entity.Field;
import com.abc.question_service.entity.Level;
import com.abc.question_service.entity.QuestionType;
import com.abc.question_service.entity.Topic;
import com.abc.question_service.model.CombinationKey;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DistributionStrategy {
    
    private static final int MINIMUM_QUESTIONS_PER_COMBINATION = 10;

    /**
     * Calculates how questions should be distributed across all valid combinations
     * of field, topic, level, and question type.
     * 
     * @param targetCount Total number of questions to generate
     * @param fields List of all fields
     * @param topics List of all topics
     * @param levels List of all levels
     * @param questionTypes List of all question types
     * @return Map of combination keys to question counts
     */
    public Map<CombinationKey, Integer> calculateDistribution(
            int targetCount,
            List<Field> fields,
            List<Topic> topics,
            List<Level> levels,
            List<QuestionType> questionTypes) {
        
        // Build valid combinations (topic must belong to field)
        List<CombinationKey> validCombinations = buildValidCombinations(
            fields, topics, levels, questionTypes
        );
        
        if (validCombinations.isEmpty()) {
            return new HashMap<>();
        }
        
        // Calculate minimum questions needed
        int totalCombinations = validCombinations.size();
        int minimumTotal = totalCombinations * MINIMUM_QUESTIONS_PER_COMBINATION;
        
        // Initialize distribution with minimum questions per combination
        Map<CombinationKey, Integer> distribution = new HashMap<>();
        for (CombinationKey key : validCombinations) {
            distribution.put(key, MINIMUM_QUESTIONS_PER_COMBINATION);
        }
        
        // If target count is less than minimum, adjust proportionally
        if (targetCount < minimumTotal) {
            return adjustToTargetCount(distribution, targetCount, validCombinations);
        }
        
        // Distribute remaining questions evenly
        int remainingCount = targetCount - minimumTotal;
        distributeRemaining(distribution, remainingCount, validCombinations);
        
        return distribution;
    }

    /**
     * Builds list of valid combinations where topic belongs to field
     */
    private List<CombinationKey> buildValidCombinations(
            List<Field> fields,
            List<Topic> topics,
            List<Level> levels,
            List<QuestionType> questionTypes) {
        
        List<CombinationKey> combinations = new ArrayList<>();
        
        // Group topics by field
        Map<Long, List<Topic>> topicsByField = new HashMap<>();
        for (Topic topic : topics) {
            Long fieldId = topic.getField().getId();
            topicsByField.computeIfAbsent(fieldId, k -> new ArrayList<>()).add(topic);
        }
        
        // Generate all valid combinations
        for (Field field : fields) {
            List<Topic> fieldTopics = topicsByField.get(field.getId());
            if (fieldTopics == null || fieldTopics.isEmpty()) {
                continue;
            }
            
            for (Topic topic : fieldTopics) {
                for (Level level : levels) {
                    for (QuestionType questionType : questionTypes) {
                        combinations.add(new CombinationKey(
                            field.getId(),
                            topic.getId(),
                            level.getId(),
                            questionType.getId()
                        ));
                    }
                }
            }
        }
        
        return combinations;
    }

    /**
     * Adjusts distribution when target count is less than minimum required
     */
    private Map<CombinationKey, Integer> adjustToTargetCount(
            Map<CombinationKey, Integer> distribution,
            int targetCount,
            List<CombinationKey> validCombinations) {
        
        Map<CombinationKey, Integer> adjusted = new HashMap<>();
        int questionsPerCombination = targetCount / validCombinations.size();
        int remainder = targetCount % validCombinations.size();
        
        for (int i = 0; i < validCombinations.size(); i++) {
            CombinationKey key = validCombinations.get(i);
            int count = questionsPerCombination + (i < remainder ? 1 : 0);
            if (count > 0) {
                adjusted.put(key, count);
            }
        }
        
        return adjusted;
    }

    /**
     * Distributes remaining questions evenly across all combinations
     */
    private void distributeRemaining(
            Map<CombinationKey, Integer> distribution,
            int remainingCount,
            List<CombinationKey> validCombinations) {
        
        if (remainingCount <= 0) {
            return;
        }
        
        int questionsPerCombination = remainingCount / validCombinations.size();
        int remainder = remainingCount % validCombinations.size();
        
        // Add base amount to all combinations
        for (CombinationKey key : validCombinations) {
            distribution.put(key, distribution.get(key) + questionsPerCombination);
        }
        
        // Distribute remainder
        for (int i = 0; i < remainder; i++) {
            CombinationKey key = validCombinations.get(i);
            distribution.put(key, distribution.get(key) + 1);
        }
    }

    /**
     * Calculates the minimum number of questions needed to satisfy
     * the minimum questions per combination requirement
     */
    public int calculateMinimumQuestions(int totalCombinations) {
        return totalCombinations * MINIMUM_QUESTIONS_PER_COMBINATION;
    }
}
