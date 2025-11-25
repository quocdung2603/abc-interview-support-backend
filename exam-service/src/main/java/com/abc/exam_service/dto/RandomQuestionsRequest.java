package com.abc.exam_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RandomQuestionsRequest {
    @NotNull(message = "Exam ID is required")
    private Long examId;
    
    // UPDATED TO USE NUMERIC IDs instead of text strings
    private Long fieldId;
    
    private List<Long> topicIds;
    
    private Long levelId;
    
    private Long questionTypeId;
    
    @NotNull(message = "Number of questions is required")
    @Min(value = 1, message = "Must request at least 1 question")
    private Integer numberOfQuestions;
}
