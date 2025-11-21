package com.abc.exam_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class RandomQuestionsResponse {
    private Long examId;
    private Integer addedCount;
    private List<Long> questionIds;
}
