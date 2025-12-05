package com.abc.question_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Progress DTO for tracking bulk question generation status.
 * Used for real-time progress monitoring.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerationProgress {
    
    /**
     * Unique identifier for the generation job.
     */
    private String jobId;
    
    /**
     * Total number of questions to be generated.
     */
    private Integer totalQuestions;
    
    /**
     * Number of questions processed so far.
     */
    private Integer processedQuestions;
    
    /**
     * Percentage of completion (0-100).
     */
    private Integer percentage;
    
    /**
     * Current status of the generation job.
     * Possible values: PENDING, IN_PROGRESS, COMPLETED, FAILED, CANCELLED
     */
    private String status;
    
    /**
     * Optional message providing additional context.
     */
    private String message;
    
    /**
     * Number of questions that failed to generate.
     */
    private Integer failedCount;
    
    /**
     * Current batch being processed.
     */
    private Integer currentBatch;
    
    /**
     * Total number of batches.
     */
    private Integer totalBatches;
}
