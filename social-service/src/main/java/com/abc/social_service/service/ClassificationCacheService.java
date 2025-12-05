package com.abc.social_service.service;

import com.abc.social_service.dto.FieldResponse;
import com.abc.social_service.dto.LevelResponse;
import com.abc.social_service.dto.TopicResponse;

import java.util.Optional;

public interface ClassificationCacheService {
    Optional<FieldResponse> getCachedField(Long fieldId);
    Optional<TopicResponse> getCachedTopic(Long topicId);
    Optional<LevelResponse> getCachedLevel(Long levelId);
    void cacheField(Long fieldId, FieldResponse field);
    void cacheTopic(Long topicId, TopicResponse topic);
    void cacheLevel(Long levelId, LevelResponse level);
    void evictAll();
}
