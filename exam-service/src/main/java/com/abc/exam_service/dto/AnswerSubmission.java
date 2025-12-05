package com.abc.exam_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnswerSubmission {
    @NotNull(message = "Question ID is required")
    private Long questionId;
    
    @NotBlank(message = "Answer content is required")
    private String answerContent;
}
