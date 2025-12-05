package com.abc.social_service.client;

import com.abc.social_service.dto.FieldResponse;
import com.abc.social_service.dto.LevelResponse;
import com.abc.social_service.dto.TopicResponse;
import com.abc.social_service.exception.ClassificationServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class QuestionServiceClientFallback implements QuestionServiceClient {
    
    @Override
    public FieldResponse getFieldById(Long fieldId) {
        log.error("Circuit breaker OPEN: Question Service unavailable for getFieldById({})", fieldId);
        throw new ClassificationServiceUnavailableException(
            "Classification validation is temporarily unavailable. Please try again later."
        );
    }
    
    @Override
    public TopicResponse getTopicById(Long topicId) {
        log.error("Circuit breaker OPEN: Question Service unavailable for getTopicById({})", topicId);
        throw new ClassificationServiceUnavailableException(
            "Classification validation is temporarily unavailable. Please try again later."
        );
    }
    
    @Override
    public LevelResponse getLevelById(Long levelId) {
        log.error("Circuit breaker OPEN: Question Service unavailable for getLevelById({})", levelId);
        throw new ClassificationServiceUnavailableException(
            "Classification validation is temporarily unavailable. Please try again later."
        );
    }
}
