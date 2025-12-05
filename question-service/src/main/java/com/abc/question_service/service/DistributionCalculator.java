package com.abc.question_service.service;

import com.abc.question_service.dto.QuestionCombination;
import com.abc.question_service.entity.Field;
import com.abc.question_service.entity.Level;
import com.abc.question_service.entity.QuestionType;
import com.abc.question_service.entity.Topic;
import com.abc.question_service.repository.FieldRepository;
import com.abc.question_service.repository.LevelRepository;
import com.abc.question_service.repository.QuestionTypeRepository;
import com.abc.question_service.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for calculating question distribution across metadata combinations.
 * Ensures balanced distribution with minimum coverage per combination.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DistributionCalculator {
    
    private final FieldRepository fieldRepository;
    private final TopicRepository topicRepository;
    private final LevelRepository levelRepository;
    private final QuestionTypeRepository questionTypeRepository;
    
    private static final int MINIMUM_QUESTIONS_PER_COMBINATION = 10;
    
    /**
     * Calculates the distribution of questions across all valid combinations.
     * 
     * @param targetCount Total number of questions to generate
     * @return List of combinations with assigned question counts
     */
    public List<QuestionCombination> calculateDistribution(int targetCount) {
        log.info("Calculating distribution for {} questions", targetCount);
        
        // Load all reference data
        List<Field> fields = fieldRepository.findAll();
        List<Topic> topics = topicRepository.findAll();
        List<Level> levels = levelRepository.findAll();
        List<QuestionType> questionTypes = questionTypeRepository.findAll();
        
        log.info("Reference data: {} fields, {} topics, {} levels, {} question types",
                fields.size(), topics.size(), levels.size(), questionTypes.size());
        
        // Generate all valid combinations
        List<QuestionCombination> combinations = generateValidCombinations(
            fields, topics, levels, questionTypes
        );
        
        log.info("Generated {} valid combinations", combinations.size());
        
        // Calculate base allocation (minimum per combination)
        int baseQuestions = combinations.size() * MINIMUM_QUESTIONS_PER_COMBINATION;
        
        if (baseQuestions > targetCount) {
            log.warn("Target count {} is less than minimum required {} ({}  combinations × {} min)",
                    targetCount, baseQuestions, combinations.size(), MINIMUM_QUESTIONS_PER_COMBINATION);
            // Distribute evenly with reduced minimum
            return distributeEvenly(combinations, targetCount);
        }
        
        // Assign minimum to each combination
        for (QuestionCombination combination : combinations) {
            combination.setQuestionCount(MINIMUM_QUESTIONS_PER_COMBINATION);
        }
        
        // Distribute remaining questions proportionally
        int remaining = targetCount - baseQuestions;
        log.info("Distributing {} remaining questions proportionally", remaining);
        
        distributeRemaining(combinations, remaining, fields, topics);
        
        // Verify total
        int total = combinations.stream()
                .mapToInt(QuestionCombination::getQuestionCount)
                .sum();
        
        log.info("Distribution complete: {} questions allocated across {} combinations",
                total, combinations.size());
        
        return combinations;
    }
    
    /**
     * Generates all valid combinations of field, topic, level, and question type.
     * Ensures topic belongs to the correct field.
     */
    private List<QuestionCombination> generateValidCombinations(
            List<Field> fields,
            List<Topic> topics,
            List<Level> levels,
            List<QuestionType> questionTypes) {
        
        List<QuestionCombination> combinations = new ArrayList<>();
        
        // Group topics by field
        Map<Long, List<Topic>> topicsByField = new HashMap<>();
        for (Topic topic : topics) {
            Long fieldId = topic.getField().getId();
            topicsByField.computeIfAbsent(fieldId, k -> new ArrayList<>()).add(topic);
        }
        
        // Generate combinations
        for (Field field : fields) {
            List<Topic> fieldTopics = topicsByField.get(field.getId());
            
            if (fieldTopics == null || fieldTopics.isEmpty()) {
                log.warn("Field '{}' has no topics, skipping", field.getName());
                continue;
            }
            
            for (Topic topic : fieldTopics) {
                for (Level level : levels) {
                    for (QuestionType questionType : questionTypes) {
                        QuestionCombination combination = QuestionCombination.builder()
                                .fieldId(field.getId())
                                .fieldName(field.getName())
                                .topicId(topic.getId())
                                .topicName(topic.getName())
                                .levelId(level.getId())
                                .levelName(level.getName())
                                .questionTypeId(questionType.getId())
                                .questionTypeName(questionType.getName())
                                .questionCount(0)
                                .build();
                        
                        combinations.add(combination);
                    }
                }
            }
        }
        
        return combinations;
    }
    
    /**
     * Distributes remaining questions proportionally based on topic count per field.
     */
    private void distributeRemaining(
            List<QuestionCombination> combinations,
            int remaining,
            List<Field> fields,
            List<Topic> topics) {
        
        if (remaining <= 0) {
            return;
        }
        
        // Calculate topic count per field
        Map<Long, Integer> topicCountByField = new HashMap<>();
        for (Topic topic : topics) {
            Long fieldId = topic.getField().getId();
            topicCountByField.merge(fieldId, 1, Integer::sum);
        }
        
        int totalTopics = topics.size();
        
        // Distribute proportionally
        Map<Long, Integer> extraByField = new HashMap<>();
        for (Field field : fields) {
            int topicCount = topicCountByField.getOrDefault(field.getId(), 0);
            if (topicCount > 0) {
                int extra = (int) Math.round((double) remaining * topicCount / totalTopics);
                extraByField.put(field.getId(), extra);
            }
        }
        
        // Allocate extras to combinations
        for (QuestionCombination combination : combinations) {
            int extra = extraByField.getOrDefault(combination.getFieldId(), 0);
            if (extra > 0) {
                // Distribute evenly among combinations for this field
                List<QuestionCombination> fieldCombinations = combinations.stream()
                        .filter(c -> c.getFieldId().equals(combination.getFieldId()))
                        .toList();
                
                int perCombination = extra / fieldCombinations.size();
                combination.setQuestionCount(combination.getQuestionCount() + perCombination);
            }
        }
        
        // Handle rounding errors by adding to random combinations
        int allocated = combinations.stream()
                .mapToInt(QuestionCombination::getQuestionCount)
                .sum();
        
        int shortfall = (MINIMUM_QUESTIONS_PER_COMBINATION * combinations.size() + remaining) - allocated;
        
        if (shortfall > 0) {
            Random random = new Random();
            for (int i = 0; i < shortfall; i++) {
                QuestionCombination combination = combinations.get(random.nextInt(combinations.size()));
                combination.setQuestionCount(combination.getQuestionCount() + 1);
            }
        }
    }
    
    /**
     * Distributes questions evenly when target is less than minimum required.
     */
    private List<QuestionCombination> distributeEvenly(
            List<QuestionCombination> combinations,
            int targetCount) {
        
        int perCombination = targetCount / combinations.size();
        int remainder = targetCount % combinations.size();
        
        for (int i = 0; i < combinations.size(); i++) {
            QuestionCombination combination = combinations.get(i);
            combination.setQuestionCount(perCombination + (i < remainder ? 1 : 0));
        }
        
        return combinations;
    }
    
    /**
     * Validates that a combination is valid (topic belongs to field).
     */
    public boolean isValidCombination(Long fieldId, Long topicId) {
        Topic topic = topicRepository.findById(topicId).orElse(null);
        if (topic == null) {
            return false;
        }
        return topic.getField().getId().equals(fieldId);
    }
}
