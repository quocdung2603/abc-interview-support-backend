package com.abc.social_service.service.impl;

import com.abc.social_service.dto.FieldResponse;
import com.abc.social_service.dto.LevelResponse;
import com.abc.social_service.dto.TopicResponse;
import com.abc.social_service.service.ClassificationCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class ClassificationCacheServiceImpl implements ClassificationCacheService {
    
    @Override
    @Cacheable(value = "classificationFields", key = "#fieldId", unless = "#result == null")
    public Optional<FieldResponse> getCachedField(Long fieldId) {
        log.debug("Cache miss for field: {}", fieldId);
        return Optional.empty();
    }
    
    @Override
    @Cacheable(value = "classificationTopics", key = "#topicId", unless = "#result == null")
    public Optional<TopicResponse> getCachedTopic(Long topicId) {
        log.debug("Cache miss for topic: {}", topicId);
        return Optional.empty();
    }
    
    @Override
    @Cacheable(value = "classificationLevels", key = "#levelId", unless = "#result == null")
    public Optional<LevelResponse> getCachedLevel(Long levelId) {
        log.debug("Cache miss for level: {}", levelId);
        return Optional.empty();
    }
    
    @Override
    @CachePut(value = "classificationFields", key = "#fieldId")
    public void cacheField(Long fieldId, FieldResponse field) {
        log.debug("Caching field: {} - {}", fieldId, field.getName());
    }
    
    @Override
    @CachePut(value = "classificationTopics", key = "#topicId")
    public void cacheTopic(Long topicId, TopicResponse topic) {
        log.debug("Caching topic: {} - {}", topicId, topic.getName());
    }
    
    @Override
    @CachePut(value = "classificationLevels", key = "#levelId")
    public void cacheLevel(Long levelId, LevelResponse level) {
        log.debug("Caching level: {} - {}", levelId, level.getName());
    }
    
    @Override
    @CacheEvict(value = {"classificationFields", "classificationTopics", "classificationLevels"}, allEntries = true)
    public void evictAll() {
        log.info("Evicting all classification caches");
    }
}
