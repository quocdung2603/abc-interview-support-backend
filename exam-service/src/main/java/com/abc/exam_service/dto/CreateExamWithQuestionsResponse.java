package com.abc.exam_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateExamWithQuestionsResponse {
    private Long examId;
    private String title;
    private String status;
    private Integer duration;
    private Integer questionCount;
    private List<Long> questionIds;
    private List<QuestionDTO> questions; // Danh sách câu hỏi chi tiết kèm đáp án
}
