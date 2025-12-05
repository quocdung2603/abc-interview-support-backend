package com.abc.social_service.service.impl;

import com.abc.social_service.dto.FieldResponse;
import com.abc.social_service.dto.LevelResponse;
import com.abc.social_service.dto.TopicResponse;
import com.abc.social_service.service.ClassificationCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "none", matchIfMissing = true)
public class ClassificationNoCacheServiceImpl implements ClassificationCacheService {
    
    @Override
    public Optional<FieldResponse> getCachedField(Long fieldId) {
        log.debug("No cache - returning empty for field: {}", fieldId);
        return Optional.empty();
    }
    
    @Override
    public Optional<TopicResponse> getCachedTopic(Long topicId) {
        log.debug("No cache - returning empty for topic: {}", topicId);
        return Optional.empty();
    }
    
    @Override
    public Optional<LevelResponse> getCachedLevel(Long levelId) {
        log.debug("No cache - returning empty for level: {}", levelId);
        return Optional.empty();
    }
    
    @Override
    public void cacheField(Long fieldId, FieldResponse field) {
        log.debug("No cache - skipping field cache: {} - {}", fieldId, field.getName());
    }
    
    @Override
    public void cacheTopic(Long topicId, TopicResponse topic) {
        log.debug("No cache - skipping topic cache: {} - {}", topicId, topic.getName());
    }
    
    @Override
    public void cacheLevel(Long levelId, LevelResponse level) {
        log.debug("No cache - skipping level cache: {} - {}", levelId, level.getName());
    }
    
    @Override
    public void evictAll() {
        log.info("No cache - nothing to evict");
    }
}
