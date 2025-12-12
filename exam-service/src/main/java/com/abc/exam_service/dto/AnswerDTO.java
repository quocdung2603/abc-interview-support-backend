package com.abc.exam_service.dto;

import lombok.Data;

@Data
public class AnswerDTO {
    private Long id;
    private Long questionId;
    private String answerContent;
    private Boolean isCorrect;
    private Integer orderNumber;
}
