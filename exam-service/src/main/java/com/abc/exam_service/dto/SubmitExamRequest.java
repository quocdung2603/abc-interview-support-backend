package com.abc.exam_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SubmitExamRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotEmpty(message = "At least one answer is required")
    @Valid
    private List<AnswerSubmission> answers;
}
