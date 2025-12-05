package com.abc.exam_service.util;

/**
 * Utility class for grading exam answers.
 * Provides methods for answer comparison, score calculation, and pass/fail determination.
 */
public class AnswerGrader {
    
    private static final double DEFAULT_PASS_THRESHOLD = 70.0;
    
    /**
     * Checks if a user's answer is correct by comparing with the correct answer.
     * Uses case-insensitive exact matching after normalizing both answers.
     * 
     * @param userAnswer the answer provided by the user
     * @param correctAnswer the correct answer to compare against
     * @return true if answers match, false otherwise
     */
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
