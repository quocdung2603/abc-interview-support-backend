package com.abc.exam_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuestionDTO {
    private Long id;
    private String field;
    private List<String> topics;
    private String level;
    private String questionType;
    private String questionText;
    private String questionAnswer; // Đáp án của câu hỏi
}
