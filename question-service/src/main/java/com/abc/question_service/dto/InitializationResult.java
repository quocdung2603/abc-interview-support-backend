package com.abc.question_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Result DTO for database initialization operations.
 * Contains counts of created/deleted entities and status information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitializationResult {
    
    /**
     * Number of fields created during initialization.
     */
    private Integer fieldsCreated;
    
    /**
     * Number of topics created during initialization.
     */
    private Integer topicsCreated;
    
    /**
     * Number of levels created during initialization.
     */
    private Integer levelsCreated;
    
    /**
     * Number of question types created during initialization.
     */
    private Integer questionTypesCreated;
    
    /**
     * Number of questions deleted during reset.
     */
    private Integer questionsDeleted;
    
    /**
     * Number of answers deleted during reset.
     */
    private Integer answersDeleted;
    
    /**
     * Overall success status of the initialization.
     */
    private Boolean success;
    
    /**
     * List of error messages encountered during initialization.
     */
    @Builder.Default
    private List<String> errors = new ArrayList<>();
    
    /**
     * Optional message providing additional context.
     */
    private String message;
}
