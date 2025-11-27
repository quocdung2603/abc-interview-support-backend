package com.abc.exam_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class ExamRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotBlank(message = "Exam type is required")
    private String examType;
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    private String position;
    
    // Numeric IDs for field, topics, level, and question types
    private Long fieldId;
    
    @NotEmpty(message = "At least one topic is required")
    private List<Long> topicIds; // Multiple topic IDs
    
    private Long levelId;
    
    @NotEmpty(message = "At least one question type is required")
    private List<Long> questionTypeIds; // Multiple question type IDs
    
    @NotNull(message = "Question count is required")
    @Min(value = 1, message = "Must have at least 1 question")
    @Max(value = 100, message = "Cannot exceed 100 questions")
    private Integer questionCount;
    
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer duration;
    
    @NotBlank(message = "Language is required")
    private String language;
}
