package com.abc.exam_service.service;

import com.abc.exam_service.dto.QuestionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionServiceClient {
    private final RestTemplate restTemplate;
    private static final String QUESTION_SERVICE_URL = "http://QUESTION-SERVICE";
    
    public List<QuestionDTO> searchQuestions(String field, List<String> topics, String level, String questionType, int limit) {
        try {
            // Request all questions (size=1000 is max) to filter in memory
            // For production, question-service should support filter params
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(QUESTION_SERVICE_URL + "/questions")
                    .queryParam("size", 1000);
            
            String url = builder.toUriString();
            log.info("Calling question service: {}", url);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    Map.class
            );
            
            Map<String, Object> pageResponse = response.getBody();
            List<Map<String, Object>> content = (List<Map<String, Object>>) pageResponse.get("content");
            
            // Convert to QuestionDTO
            List<QuestionDTO> allQuestions = content.stream()
                    .map(this::mapToQuestionDTO)
                    .toList(); // Get all first
            
            // Filter in memory
            List<QuestionDTO> questions = allQuestions.stream()
                    .filter(q -> matchesCriteria(q, field, topics, level, questionType))
                    .limit(limit)
                    .collect(java.util.stream.Collectors.toList()); // Use Collectors.toList() for mutable list
            
            log.info("Found {} questions matching criteria out of {} total", questions.size(), allQuestions.size());
            return questions;
        } catch (Exception e) {
            log.error("Error calling question service", e);
            throw new RuntimeException("Failed to fetch questions from question service", e);
        }
    }
    
    public QuestionDTO getQuestionById(Long questionId) {
        return getQuestionByIdWithRetry(questionId, 3, 1000);
    }
    
    /**
     * Fetches a question by ID with retry logic and exponential backoff.
     * 
     * @param questionId the ID of the question to fetch
     * @param maxAttempts maximum number of retry attempts (default: 3)
     * @param initialDelayMs initial delay in milliseconds (default: 1000)
     * @return QuestionDTO with question details including correct answer
     * @throws RuntimeException if all retry attempts fail
     */
    public QuestionDTO getQuestionByIdWithRetry(Long questionId, int maxAttempts, long initialDelayMs) {
        int attempt = 0;
        long delay = initialDelayMs;
        Exception lastException = null;
        
        while (attempt < maxAttempts) {
            try {
                String url = QUESTION_SERVICE_URL + "/questions/" + questionId;
                log.debug("Fetching question by ID (attempt {}): {}", attempt + 1, url);
                
                ResponseEntity<Map> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        Map.class
                );
                
                Map<String, Object> questionData = response.getBody();
                QuestionDTO result = mapToQuestionDTO(questionData);
                
                if (attempt > 0) {
                    log.info("Successfully fetched question {} after {} attempts", questionId, attempt + 1);
                }
                
                return result;
            } catch (Exception e) {
                lastException = e;
                attempt++;
                
                if (attempt < maxAttempts) {
                    log.warn("Failed to fetch question {} (attempt {}), retrying in {}ms: {}", 
                            questionId, attempt, delay, e.getMessage());
                    
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted while waiting to retry", ie);
                    }
                    
                    // Exponential backoff: double the delay for next attempt
                    delay *= 2;
                } else {
                    log.error("Failed to fetch question {} after {} attempts", questionId, maxAttempts, e);
                }
            }
        }
        
        throw new RuntimeException("Failed to fetch question " + questionId + " after " + maxAttempts + " attempts", lastException);
    }
    
    private QuestionDTO mapToQuestionDTO(Map<String, Object> map) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(((Number) map.get("id")).longValue());
        
        // Map numeric IDs only (encoding independent)
        if (map.get("fieldId") != null) {
            dto.setFieldId(((Number) map.get("fieldId")).longValue());
        }
        if (map.get("topicId") != null) {
            dto.setTopicId(((Number) map.get("topicId")).longValue());
        }
        if (map.get("levelId") != null) {
            dto.setLevelId(((Number) map.get("levelId")).longValue());
        }
        if (map.get("questionTypeId") != null) {
            dto.setQuestionTypeId(((Number) map.get("questionTypeId")).longValue());
        }
        
        // Map question content and answer
        dto.setQuestionText((String) map.get("questionContent"));
        dto.setQuestionAnswer((String) map.get("questionAnswer"));
        
        return dto;
    }
    
    // Deprecated: This method uses text-based matching which has encoding issues
    // Use searchQuestionsByIds instead for numeric ID-based matching
    private boolean matchesCriteria(QuestionDTO q, String field, List<String> topics, String level, String questionType) {
        // This method is deprecated and should not be used
        // All filtering should be done by question-service using numeric IDs
        return true;
    }
    
    // Validate if field exists
    public boolean fieldExists(Long fieldId) {
        try {
            String url = QUESTION_SERVICE_URL + "/fields/" + fieldId;
            log.info("Checking if field exists: {}", url);
            restTemplate.getForEntity(url, Map.class);
            log.info("Field {} exists", fieldId);
            return true;
        } catch (Exception e) {
            log.error("Field {} validation failed: {}", fieldId, e.getMessage());
            return false;
        }
    }
    
    // Validate if topic exists
    public boolean topicExists(Long topicId) {
        try {
            String url = QUESTION_SERVICE_URL + "/topics/" + topicId;
            log.debug("Checking if topic exists: {}", url);
            restTemplate.getForEntity(url, Map.class);
            return true;
        } catch (Exception e) {
            log.debug("Topic {} does not exist", topicId);
            return false;
        }
    }
    
    // Validate if level exists
    public boolean levelExists(Long levelId) {
        try {
            String url = QUESTION_SERVICE_URL + "/levels/" + levelId;
            log.debug("Checking if level exists: {}", url);
            restTemplate.getForEntity(url, Map.class);
            return true;
        } catch (Exception e) {
            log.debug("Level {} does not exist", levelId);
            return false;
        }
    }
    
    // NEW: Search questions by numeric IDs
    public List<QuestionDTO> searchQuestionsByIds(Long fieldId, List<Long> topicIds, Long levelId, Long questionTypeId, int limit) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(QUESTION_SERVICE_URL + "/questions/search");
            
            if (fieldId != null) {
                builder.queryParam("fieldId", fieldId);
            }
            if (topicIds != null && !topicIds.isEmpty()) {
                builder.queryParam("topicIds", topicIds.toArray());
            }
            if (levelId != null) {
                builder.queryParam("levelId", levelId);
            }
            if (questionTypeId != null) {
                builder.queryParam("questionTypeId", questionTypeId);
            }
            if (limit > 0) {
                builder.queryParam("limit", limit);
            }
            
            String url = builder.toUriString();
            log.info("Calling question service search by IDs: {}", url);
            
            ResponseEntity<List<Map>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map>>() {}
            );
            
            List<Map<String, Object>> content = (List<Map<String, Object>>) (List<?>) response.getBody();
            
            // Convert to QuestionDTO
            List<QuestionDTO> questions = content.stream()
                    .map(this::mapToQuestionDTO)
                    .collect(java.util.stream.Collectors.toList());
            
            log.info("Found {} questions matching ID criteria", questions.size());
            return questions;
        } catch (Exception e) {
            log.error("Error calling question service search by IDs", e);
            throw new RuntimeException("Failed to search questions by IDs from question service", e);
        }
    }
    
    /**
     * Batch fetches multiple questions by their IDs.
     * More efficient than individual calls when fetching many questions.
     * 
     * @param questionIds list of question IDs to fetch
     * @return Map of questionId -> QuestionDTO
     */
    public Map<Long, QuestionDTO> getQuestionsWithAnswers(List<Long> questionIds) {
        Map<Long, QuestionDTO> result = new HashMap<>();
        
        if (questionIds == null || questionIds.isEmpty()) {
            return result;
        }
        
        log.info("Batch fetching {} questions", questionIds.size());
        
        for (Long questionId : questionIds) {
            try {
                QuestionDTO question = getQuestionById(questionId);
                result.put(questionId, question);
            } catch (Exception e) {
                log.error("Failed to fetch question {} in batch operation", questionId, e);
                // Continue with other questions even if one fails
            }
        }
        
        log.info("Successfully fetched {}/{} questions", result.size(), questionIds.size());
        return result;
    }
    
    /**
     * Fetches all answers for a specific question.
     * 
     * @param questionId the question ID
     * @return List of AnswerDTO
     */
    public List<com.abc.exam_service.dto.AnswerDTO> getAnswersByQuestionId(Long questionId) {
        try {
            String url = QUESTION_SERVICE_URL + "/questions/" + questionId + "/answers";
            log.debug("Fetching answers for question {}: {}", questionId, url);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    Map.class
            );
            
            Map<String, Object> pageResponse = response.getBody();
            if (pageResponse == null || !pageResponse.containsKey("content")) {
                log.warn("No answers found for question {}", questionId);
                return java.util.Collections.emptyList();
            }
            
            List<Map<String, Object>> answersData = (List<Map<String, Object>>) pageResponse.get("content");
            if (answersData == null || answersData.isEmpty()) {
                log.warn("No answers found for question {}", questionId);
                return java.util.Collections.emptyList();
            }
            
            return answersData.stream()
                    .map(this::mapToAnswerDTO)
                    .collect(java.util.stream.Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error fetching answers for question {}", questionId, e);
            return java.util.Collections.emptyList();
        }
    }
    
    private com.abc.exam_service.dto.AnswerDTO mapToAnswerDTO(Map<String, Object> map) {
        com.abc.exam_service.dto.AnswerDTO dto = new com.abc.exam_service.dto.AnswerDTO();
        dto.setId(((Number) map.get("id")).longValue());
        dto.setQuestionId(((Number) map.get("questionId")).longValue());
        dto.setAnswerContent((String) map.get("answerContent"));
        dto.setIsCorrect((Boolean) map.get("isCorrect"));
        if (map.get("orderNumber") != null) {
            dto.setOrderNumber(((Number) map.get("orderNumber")).intValue());
        }
        return dto;
    }
}
