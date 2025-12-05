package com.abc.social_service.service;

import com.abc.social_service.dto.FieldResponse;
import com.abc.social_service.dto.LevelResponse;
import com.abc.social_service.dto.TopicResponse;
import com.abc.social_service.service.impl.ClassificationCacheServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ClassificationCacheServiceTest {
    
    @Autowired
    private ClassificationCacheService cacheService;
    
    @Autowired
    private CacheManager cacheManager;
    
    @BeforeEach
    void setUp() {
        // Clear all caches before each test
        cacheService.evictAll();
    }
    
    @Test
    void testCacheMissReturnsEmpty() {
        // When: Getting a field that's not in cache
        Optional<FieldResponse> result = cacheService.getCachedField(999L);
        
        // Then: Should return empty
        assertThat(result).isEmpty();
    }
    
    @Test
    void testCacheFieldAndRetrieve() {
        // Given: A field response
        FieldResponse field = new FieldResponse(1L, "Computer Science", "CS field");
        
        // When: Caching the field
        cacheService.cacheField(1L, field);
        
        // Then: Should be able to retrieve from cache
        Optional<FieldResponse> cached = cacheService.getCachedField(1L);
        assertThat(cached).isPresent();
        assertThat(cached.get().getId()).isEqualTo(1L);
        assertThat(cached.get().getName()).isEqualTo("Computer Science");
    }
    
    @Test
    void testCacheTopicAndRetrieve() {
        // Given: A topic response
        TopicResponse topic = new TopicResponse(5L, 1L, "Computer Science", "Data Structures", "DS topic");
        
        // When: Caching the topic
        cacheService.cacheTopic(5L, topic);
        
        // Then: Should be able to retrieve from cache
        Optional<TopicResponse> cached = cacheService.getCachedTopic(5L);
        assertThat(cached).isPresent();
        assertThat(cached.get().getId()).isEqualTo(5L);
        assertThat(cached.get().getName()).isEqualTo("Data Structures");
    }
    
    @Test
    void testCacheLevelAndRetrieve() {
        // Given: A level response
        LevelResponse level = new LevelResponse(2L, "Intermediate", "Mid level");
        
        // When: Caching the level
        cacheService.cacheLevel(2L, level);
        
        // Then: Should be able to retrieve from cache
        Optional<LevelResponse> cached = cacheService.getCachedLevel(2L);
        assertThat(cached).isPresent();
        assertThat(cached.get().getId()).isEqualTo(2L);
        assertThat(cached.get().getName()).isEqualTo("Intermediate");
    }
    
    @Test
    void testEvictAllClearsAllCaches() {
        // Given: Cached data
        FieldResponse field = new FieldResponse(1L, "Math", "Mathematics");
        TopicResponse topic = new TopicResponse(5L, 1L, "Math", "Calculus", "Calc");
        LevelResponse level = new LevelResponse(2L, "Advanced", "High level");
        
        cacheService.cacheField(1L, field);
        cacheService.cacheTopic(5L, topic);
        cacheService.cacheLevel(2L, level);
        
        // When: Evicting all caches
        cacheService.evictAll();
        
        // Then: All caches should be empty
        assertThat(cacheService.getCachedField(1L)).isEmpty();
        assertThat(cacheService.getCachedTopic(5L)).isEmpty();
        assertThat(cacheService.getCachedLevel(2L)).isEmpty();
    }
}
