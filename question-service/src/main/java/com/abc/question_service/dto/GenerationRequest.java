package com.abc.question_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerationRequest {
    
    @NotNull(message = "Target count is required")
    @Min(value = 1, message = "Target count must be at least 1")
    @Max(value = 100000, message = "Target count cannot exceed 100,000")
    private Integer targetCount;
    
    @Min(value = 10, message = "Batch size must be at least 10")
    @Max(value = 1000, message = "Batch size cannot exceed 1,000")
    private Integer batchSize = 100; // Default batch size
    
    @NotNull(message = "Default user ID is required")
    @Min(value = 1, message = "User ID must be positive")
    private Long defaultUserId;
    
    @NotNull(message = "Default approver ID is required")
    @Min(value = 1, message = "Approver ID must be positive")
    private Long defaultApproverId;
}
