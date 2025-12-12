package com.abc.exam_service.util;

import com.abc.exam_service.dto.AnswerDTO;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for grading exam answers.
 * Provides methods for answer comparison, score calculation, and pass/fail determination.
 * Supports SingleChoice, MultipleChoice, and Essay question types.
 */
public class AnswerGrader {
    
    private static final double DEFAULT_PASS_THRESHOLD = 70.0;
    private static final int MIN_ESSAY_LENGTH = 10;
    
    /**
     * Grades a SingleChoice question (questionTypeId = 1).
     * User selects one answer by ID.
     * 
     * @param userAnswerContent the answer ID selected by user (e.g., "12")
     * @param answers list of all answers for the question
     * @return true if the selected answer has isCorrect = true
     */
    public static boolean gradeSingleChoice(String userAnswerContent, List<AnswerDTO> answers) {
        if (userAnswerContent == null || userAnswerContent.trim().isEmpty() || answers == null || answers.isEmpty()) {
            return false;
        }
        
        try {
            Long selectedAnswerId = Long.parseLong(userAnswerContent.trim());
            return answers.stream()
                    .anyMatch(answer -> answer.getId().equals(selectedAnswerId) && Boolean.TRUE.equals(answer.getIsCorrect()));
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Grades a MultipleChoice question (questionTypeId = 2).
     * User selects multiple answers by IDs separated by semicolon.
     * User must select ALL correct answers and NO incorrect answers.
     * 
     * @param userAnswerContent the answer IDs selected by user (e.g., "14;15")
     * @param answers list of all answers for the question
     * @return true if user selected exactly all correct answers
     */
    public static boolean gradeMultipleChoice(String userAnswerContent, List<AnswerDTO> answers) {
        if (userAnswerContent == null || userAnswerContent.trim().isEmpty() || answers == null || answers.isEmpty()) {
            return false;
        }
        
        // Parse user selected answer IDs
        Set<Long> userSelectedIds;
        try {
            userSelectedIds = Arrays.stream(userAnswerContent.split(";"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
        } catch (NumberFormatException e) {
            return false;
        }
        
        // Get all correct answer IDs
        Set<Long> correctAnswerIds = answers.stream()
                .filter(answer -> Boolean.TRUE.equals(answer.getIsCorrect()))
                .map(AnswerDTO::getId)
                .collect(Collectors.toSet());
        
        // User must select exactly all correct answers (no more, no less)
        return userSelectedIds.equals(correctAnswerIds);
    }
    
    /**
     * Grades an Essay question (questionTypeId = 3).
     * Checks if answer is not empty and has at least 10 characters.
     * 
     * @param userAnswerContent the essay content provided by user
     * @return true if answer length >= 10 characters
     */
    public static boolean gradeEssay(String userAnswerContent) {
        if (userAnswerContent == null || userAnswerContent.trim().isEmpty()) {
            return false;
        }
        
        return userAnswerContent.trim().length() >= MIN_ESSAY_LENGTH;
    }
    
    /**
     * Checks if a user's answer is correct by comparing with the correct answer.
     * Uses case-insensitive exact matching after normalizing both answers.
     * 
     * @param userAnswer the answer provided by the user
     * @param correctAnswer the correct answer to compare against
     * @return true if answers match, false otherwise
     * @deprecated Use question-type-specific grading methods instead
     */
    @Deprecated
    public static boolean isCorrect(String userAnswer, String correctAnswer) {
        if (userAnswer == null || correctAnswer == null) {
            return false;
        }
        
        String normalizedUserAnswer = normalizeAnswer(userAnswer);
        String normalizedCorrectAnswer = normalizeAnswer(correctAnswer);
        
        return normalizedUserAnswer.equals(normalizedCorrectAnswer);
    }
    
    /**
     * Normalizes an answer by converting to lowercase and trimming whitespace.
     * 
     * @param answer the answer to normalize
     * @return normalized answer
     */
    private static String normalizeAnswer(String answer) {
        if (answer == null) {
            return "";
        }
        return answer.trim().toLowerCase();
    }
    
    /**
     * Calculates the percentage score based on correct and total answer counts.
     * 
     * @param correctCount number of correct answers
     * @param totalCount total number of questions
     * @return percentage score (0.0 to 100.0)
     */
    public static double calculateScore(int correctCount, int totalCount) {
        if (totalCount <= 0) {
            return 0.0;
        }
        return ((double) correctCount / totalCount) * 100.0;
    }
    
    /**
     * Determines if a score meets the pass threshold.
     * 
     * @param score the score to check
     * @param threshold the minimum score required to pass
     * @return true if score >= threshold, false otherwise
     */
    public static boolean determinePassStatus(double score, double threshold) {
        return score >= threshold;
    }
    
    /**
     * Determines if a score meets the default pass threshold (70%).
     * 
     * @param score the score to check
     * @return true if score >= 70%, false otherwise
     */
    public static boolean determinePassStatus(double score) {
        return determinePassStatus(score, DEFAULT_PASS_THRESHOLD);
    }
}
