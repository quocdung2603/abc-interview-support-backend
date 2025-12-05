package com.abc.exam_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnswerHistoryItem {
    private Long questionId;
    private Integer orderNumber;
    private String questionContent;
    private String userAnswer;
    private String correctAnswer;
    private Boolean isCorrect;
    private QuestionMetadata metadata;
}
