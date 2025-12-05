package com.abc.exam_service.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ExamHistoryResponse {
    private Long examId;
    private Long userId;
    private String examTitle;
    private Double score;
    private Boolean passStatus;
    private LocalDateTime completedAt;
    private List<AnswerHistoryItem> answers;
}
