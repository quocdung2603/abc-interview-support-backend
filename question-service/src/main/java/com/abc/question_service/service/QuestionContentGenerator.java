package com.abc.question_service.service;

import com.abc.question_service.dto.QuestionContent;
import com.abc.question_service.entity.Field;
import com.abc.question_service.entity.Level;
import com.abc.question_service.entity.QuestionType;
import com.abc.question_service.entity.Topic;

import java.util.Set;

/**
 * Service interface for generating unique question content.
 * Uses templates and topic-specific terminology to create realistic questions.
 */
public interface QuestionContentGenerator {
    
    /**
     * Generates a unique question with content and answer.
     * 
     * @param field The field for the question
     * @param topic The topic for the question
     * @param level The difficulty level
     * @param questionType The type of question (Single/Multiple/Fill)
     * @param existingContent Set of existing question content to avoid duplicates
     * @return QuestionContent with generated question and answer
     */
    QuestionContent generateQuestion(
        Field field,
        Topic topic,
        Level level,
        QuestionType questionType,
        Set<String> existingContent
    );
    
    /**
     * Checks if the given content is unique.
     * 
     * @param content The content to check
     * @param existingContent Set of existing content
     * @return true if unique, false otherwise
     */
    boolean isUnique(String content, Set<String> existingContent);
}
