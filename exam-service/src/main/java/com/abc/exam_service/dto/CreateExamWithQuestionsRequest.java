package com.abc.exam_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class CreateExamWithQuestionsRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    private String position;
    
    @NotNull(message = "Duration is required (in minutes)")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer duration;
    
    @NotBlank(message = "Language is required")
    private String language;
    
    // Question criteria for random selection - UPDATED TO USE NUMERIC IDs
    private Long fieldId;
    
    private List<Long> topicIds;
    
    private Long levelId;
    
    private Long questionTypeId;
    
    @NotNull(message = "Number of questions is required")
    @Min(value = 1, message = "Must have at least 1 question")
    @Max(value = 100, message = "Cannot exceed 100 questions")
    private Integer numberOfQuestions;
}
