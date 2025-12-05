package com.abc.social_service.client;

import com.abc.social_service.dto.FieldResponse;
import com.abc.social_service.dto.LevelResponse;
import com.abc.social_service.dto.TopicResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "question-service",
    url = "${question-service.url}",
    fallback = QuestionServiceClientFallback.class
)
public interface QuestionServiceClient {
    
    @GetMapping("/questions/fields/{id}")
    @CircuitBreaker(name = "questionService")
    FieldResponse getFieldById(@PathVariable("id") Long fieldId);
    
    @GetMapping("/questions/topics/{id}")
    @CircuitBreaker(name = "questionService")
    TopicResponse getTopicById(@PathVariable("id") Long topicId);
    
    @GetMapping("/questions/levels/{id}")
    @CircuitBreaker(name = "questionService")
    LevelResponse getLevelById(@PathVariable("id") Long levelId);
}
