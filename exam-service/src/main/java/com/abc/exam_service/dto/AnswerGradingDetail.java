package com.abc.exam_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerGradingDetail {
    private Long questionId;
    private String userAnswer;
    private Boolean isCorrect;
    private String correctAnswer;
}
