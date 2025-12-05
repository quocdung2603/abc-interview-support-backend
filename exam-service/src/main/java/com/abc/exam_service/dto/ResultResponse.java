package com.abc.exam_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultResponse {
    private Long id;
    private Long examId;
    private Long userId;
    private Double score;
    private Boolean passStatus;
    private String feedback;
    private LocalDateTime completedAt;
}
