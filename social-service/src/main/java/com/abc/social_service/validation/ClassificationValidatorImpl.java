package com.abc.social_service.validation;

import com.abc.social_service.client.QuestionServiceClient;
import com.abc.social_service.dto.FieldResponse;
import com.abc.social_service.dto.LevelResponse;
import com.abc.social_service.dto.TopicResponse;
import com.abc.social_service.service.ClassificationCacheService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClassificationValidatorImpl implements ClassificationValidator {
    
    private final QuestionServiceClient questionServiceClient;
    private final ClassificationCacheService cacheService;
    
    @Override
    public ValidationResult validateClassification(Long fieldId, Long topicId, Long levelId) {
        Map<String, String> fieldErrors = new HashMap<>();
        
        // Validate fieldId
        if (!validateFieldExists(fieldId, fieldErrors)) {
            return ValidationResult.failure("Invalid classification", fieldErrors);
        }
        
        // Validate topicId and check if it belongs to fieldId
        if (!validateTopicBelongsToField(topicId, fieldId, fieldErrors)) {
            return ValidationResult.failure("Invalid classification", fieldErrors);
        }
        
        // Validate levelId (optional)
        if (levelId != null && !validateLevelExists(levelId, fieldErrors)) {
            return ValidationResult.failure("Invalid classification", fieldErrors);
        }
        
        return ValidationResult.success();
    }
    
    private boolean validateFieldExists(Long fieldId, Map<String, String> fieldErrors) {
        try {
            // Check cache first
            var cachedField = cacheService.getCachedField(fieldId);
            if (cachedField.isPresent()) {
                log.debug("Field {} found in cache", fieldId);
                return true;
            }
            
            // Call Question Service
            FieldResponse field = questionServiceClient.getFieldById(fieldId);
            if (field != null && field.getId() != null) {
                cacheService.cacheField(fieldId, field);
                log.debug("Field {} validated and cached", fieldId);
                return true;
            }
            
            fieldErrors.put("fieldId", "Field with ID " + fieldId + " does not exist");
            return false;
            
        } catch (FeignException.NotFound e) {
            log.warn("Field {} not found", fieldId);
            fieldErrors.put("fieldId", "Field with ID " + fieldId + " does not exist");
            return false;
        } catch (Exception e) {
            log.error("Error validating field {}: {}", fieldId, e.getMessage());
            throw e;
        }
    }
    
    private boolean validateTopicBelongsToField(Long topicId, Long fieldId, Map<String, String> fieldErrors) {
        try {
            // Check cache first
            var cachedTopic = cacheService.getCachedTopic(topicId);
            if (cachedTopic.isPresent()) {
                TopicResponse topic = cachedTopic.get();
                if (!topic.getFieldId().equals(fieldId)) {
                    fieldErrors.put("topicId", 
                        "Topic with ID " + topicId + " does not belong to field " + fieldId);
                    return false;
                }
                log.debug("Topic {} found in cache and belongs to field {}", topicId, fieldId);
                return true;
            }
            
            // Call Question Service
            TopicResponse topic = questionServiceClient.getTopicById(topicId);
            if (topic != null && topic.getId() != null) {
                cacheService.cacheTopic(topicId, topic);
                
                if (!topic.getFieldId().equals(fieldId)) {
                    fieldErrors.put("topicId", 
                        "Topic with ID " + topicId + " does not belong to field " + fieldId);
                    return false;
                }
                
                log.debug("Topic {} validated and cached", topicId);
                return true;
            }
            
            fieldErrors.put("topicId", "Topic with ID " + topicId + " does not exist");
            return false;
            
        } catch (FeignException.NotFound e) {
            log.warn("Topic {} not found", topicId);
            fieldErrors.put("topicId", "Topic with ID " + topicId + " does not exist");
            return false;
        } catch (Exception e) {
            log.error("Error validating topic {}: {}", topicId, e.getMessage());
            throw e;
        }
    }
    
    private boolean validateLevelExists(Long levelId, Map<String, String> fieldErrors) {
        try {
            // Check cache first
            var cachedLevel = cacheService.getCachedLevel(levelId);
            if (cachedLevel.isPresent()) {
                log.debug("Level {} found in cache", levelId);
                return true;
            }
            
            // Call Question Service
            LevelResponse level = questionServiceClient.getLevelById(levelId);
            if (level != null && level.getId() != null) {
                cacheService.cacheLevel(levelId, level);
                log.debug("Level {} validated and cached", levelId);
                return true;
            }
            
            fieldErrors.put("levelId", "Level with ID " + levelId + " does not exist");
            return false;
            
        } catch (FeignException.NotFound e) {
            log.warn("Level {} not found", levelId);
            fieldErrors.put("levelId", "Level with ID " + levelId + " does not exist");
            return false;
        } catch (Exception e) {
            log.error("Error validating level {}: {}", levelId, e.getMessage());
            throw e;
        }
    }
}
