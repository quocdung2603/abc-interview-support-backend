package com.abc.question_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for bulk question generation.
 * Contains parameters to control the generation process.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkGenerationRequest {
    
    /**
     * Target number of questions to generate.
     * Must be between 1 and 100,000.
     */
    @NotNull(message = "Target count is required")
    @Min(value = 1, message = "Target count must be at least 1")
    @Max(value = 100000, message = "Target count cannot exceed 100,000")
    private Integer targetCount = 12000;
    
    /**
     * Number of questions to process in each batch.
     * Must be between 1 and 1,000.
     */
    @NotNull(message = "Batch size is required")
    @Min(value = 1, message = "Batch size must be at least 1")
    @Max(value = 1000, message = "Batch size cannot exceed 1,000")
    private Integer batchSize = 100;
    
    /**
     * User ID to assign as the creator of generated questions.
     * Must be a positive number.
     */
    @NotNull(message = "Default user ID is required")
    @Positive(message = "User ID must be positive")
    private Long defaultUserId = 1L;
    
    /**
     * User ID to assign as the approver of generated questions.
     * Must be a positive number.
     */
    @NotNull(message = "Default approver ID is required")
    @Positive(message = "Approver ID must be positive")
    private Long defaultApproverId = 1L;
    
    /**
     * If true, validates the generation process without persisting data.
     * Useful for testing and validation.
     */
    private Boolean dryRun = false;
}
