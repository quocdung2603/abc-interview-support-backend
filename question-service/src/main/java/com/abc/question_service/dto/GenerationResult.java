package com.abc.question_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Result DTO for bulk question generation.
 * Contains comprehensive statistics and status information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerationResult {
    
    /**
     * Number of questions requested to be generated.
     */
    private Integer requestedCount;
    
    /**
     * Number of questions successfully generated.
     */
    private Integer generatedCount;
    
    /**
     * Number of questions that failed to generate.
     */
    private Integer failedCount;
    
    /**
     * Timestamp when generation started.
     */
    private LocalDateTime startTime;
    
    /**
     * Timestamp when generation completed.
     */
    private LocalDateTime endTime;
    
    /**
     * Human-readable duration string (e.g., "5 minutes 30 seconds").
     */
    private String duration;
    
    /**
     * Distribution of generated questions by field name.
     * Key: Field name, Value: Question count
     */
    @Builder.Default
    private Map<String, Integer> distributionByField = new HashMap<>();
    
    /**
     * Distribution of generated questions by level name.
     * Key: Level name, Value: Question count
     */
    @Builder.Default
    private Map<String, Integer> distributionByLevel = new HashMap<>();
    
    /**
     * Distribution of generated questions by question type name.
     * Key: Question type name, Value: Question count
     */
    @Builder.Default
    private Map<String, Integer> distributionByQuestionType = new HashMap<>();
    
    /**
     * List of error messages encountered during generation.
     */
    @Builder.Default
    private List<String> errors = new ArrayList<>();
    
    /**
     * Overall success status of the generation process.
     */
    private Boolean success;
    
    /**
     * Optional job ID for tracking async operations.
     */
    private String jobId;
}
