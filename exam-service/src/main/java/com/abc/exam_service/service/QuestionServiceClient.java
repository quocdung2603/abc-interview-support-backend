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
        try {
            String url = QUESTION_SERVICE_URL + "/questions/" + questionId;
            log.debug("Fetching question by ID: {}", url);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    Map.class
            );
            
            Map<String, Object> questionData = response.getBody();
            return mapToQuestionDTO(questionData);
        } catch (Exception e) {
            log.error("Error fetching question {}", questionId, e);
            throw new RuntimeException("Failed to fetch question " + questionId, e);
        }
    }
    
    private QuestionDTO mapToQuestionDTO(Map<String, Object> map) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(((Number) map.get("id")).longValue());
        dto.setField((String) map.get("fieldName"));
        dto.setLevel((String) map.get("levelName"));
        dto.setQuestionType((String) map.get("questionTypeName"));
        dto.setQuestionText((String) map.get("questionContent"));
        dto.setQuestionAnswer((String) map.get("questionAnswer")); // Thêm answer
        
        // Topics - might be a list or single topic
        String topicName = (String) map.get("topicName");
        if (topicName != null) {
            dto.setTopics(List.of(topicName));
        }
        
        return dto;
    }
    
    private boolean matchesCriteria(QuestionDTO q, String field, List<String> topics, String level, String questionType) {
        if (field != null && !field.isEmpty() && !field.equalsIgnoreCase(q.getField())) {
            return false;
        }
        if (level != null && !level.isEmpty() && !level.equalsIgnoreCase(q.getLevel())) {
            return false;
        }
        if (questionType != null && !questionType.isEmpty() && !questionType.equalsIgnoreCase(q.getQuestionType())) {
            return false;
        }
        if (topics != null && !topics.isEmpty()) {
            if (q.getTopics() == null || q.getTopics().isEmpty()) {
                return false;
            }
            boolean hasMatchingTopic = topics.stream()
                    .anyMatch(t -> q.getTopics().stream().anyMatch(qt -> qt.equalsIgnoreCase(t)));
            if (!hasMatchingTopic) {
                return false;
            }
        }
        return true;
    }
}
