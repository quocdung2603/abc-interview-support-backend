package com.abc.exam_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamGradingResponse {
    private Long examId;
    private Long userId;
    private Double score;
    private Boolean passStatus;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer incorrectAnswers;
    private LocalDateTime completedAt;
    private List<AnswerGradingDetail> details;
}
