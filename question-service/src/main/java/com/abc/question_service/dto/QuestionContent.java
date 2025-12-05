package com.abc.question_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for generated question content.
 * Contains the question text and answer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionContent {
    
    /**
     * The generated question text.
     */
    private String questionText;
    
    /**
     * The generated answer text.
     */
    private String answerText;
}
