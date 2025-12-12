package com.abc.exam_service.service;

import com.abc.exam_service.dto.*;
import com.abc.exam_service.entity.*;
import com.abc.exam_service.exception.InvalidRequestException;
import com.abc.exam_service.exception.ResourceNotFoundException;
import com.abc.exam_service.repository.*;
import com.abc.exam_service.util.AnswerGrader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GradingService {
    
    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamRegistrationRepository examRegistrationRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final ResultRepository resultRepository;
    private final QuestionServiceClient questionServiceClient;
    
    private static final double PASS_THRESHOLD = 70.0;
    
    /**
     * Validates that an exam exists and is in a valid status for submission.
     * 
     * @param examId the exam ID to validate
     * @return the Exam entity if valid
     * @throws ResourceNotFoundException if exam not found
     * @throws InvalidRequestException if exam status is invalid
     */
    private Exam validateExam(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));
        
        if (!"PUBLISHED".equals(exam.getStatus()) && !"COMPLETED".equals(exam.getStatus())) {
            throw new InvalidRequestException(
                    "Exam is not available for submission. Current status: " + exam.getStatus());
        }
        
        return exam;
    }
    
    /**
     * Validates that a user is registered for an exam.
     * PRACTICE and VIRTUAL exams do not require registration.
     * Only RECRUITER exams require registration.
     * 
     * @param exam the Exam entity
     * @param userId the user ID
     * @throws InvalidRequestException if user is not registered (for RECRUITER exams)
     */
    private void validateRegistration(Exam exam, Long userId) {
        // Skip registration check for PRACTICE and VIRTUAL exams
        // Only RECRUITER exams require registration
        if (!"RECRUITER".equalsIgnoreCase(exam.getExamType())) {
            log.info("Skipping registration validation for {} exam: {}", exam.getExamType(), exam.getId());
            return;
        }
        
        boolean isRegistered = examRegistrationRepository
                .existsByExamIdAndUserIdAndRegistrationStatus(exam.getId(), userId, "REGISTERED");
        
        if (!isRegistered) {
            throw new InvalidRequestException("User is not registered for this exam");
        }
    }
    
    /**
     * Validates that all submitted question IDs belong to the exam.
     * 
     * @param examId the exam ID
     * @param submittedQuestionIds the list of question IDs from submission
     * @throws InvalidRequestException if any question ID is invalid
     */
    private void validateQuestionIds(Long examId, List<Long> submittedQuestionIds) {
        List<ExamQuestion> examQuestions = examQuestionRepository.findByExamIdOrderByOrderNumberAsc(examId);
        Set<Long> validQuestionIds = examQuestions.stream()
                .map(ExamQuestion::getQuestionId)
                .collect(Collectors.toSet());
        
        List<Long> invalidIds = submittedQuestionIds.stream()
                .filter(id -> !validQuestionIds.contains(id))
                .collect(Collectors.toList());
        
        if (!invalidIds.isEmpty()) {
            throw new InvalidRequestException(
                    "Invalid question IDs: " + invalidIds + ". These questions are not part of the exam");
        }
    }
    
    /**
     * Submits and grades an exam for a user.
     * 
     * @param examId the exam ID
     * @param userId the user ID
     * @param answers the list of answer submissions
     * @return ExamGradingResponse with score, pass status, and detailed results
     */
    @Transactional
    public ExamGradingResponse submitAndGradeExam(Long examId, Long userId, List<AnswerSubmission> answers) {
        log.info("Grading exam {} for user {}", examId, userId);
        
        // Step 1: Validate exam, registration, and question IDs
        Exam exam = validateExam(examId);
        validateRegistration(exam, userId);
        
        List<Long> submittedQuestionIds = answers.stream()
                .map(AnswerSubmission::getQuestionId)
                .collect(Collectors.toList());
        validateQuestionIds(examId, submittedQuestionIds);
        
        // Step 2: Fetch questions and answers from Question Service
        log.info("Fetching questions and answers for {} questions", submittedQuestionIds.size());
        Map<Long, QuestionDTO> questions = questionServiceClient.getQuestionsWithAnswers(submittedQuestionIds);
        
        if (questions.size() < submittedQuestionIds.size()) {
            log.warn("Could not fetch all questions. Expected {}, got {}", 
                    submittedQuestionIds.size(), questions.size());
        }
        
        // Step 3: Grade each answer based on question type
        List<AnswerGradingDetail> gradingDetails = new ArrayList<>();
        int correctCount = 0;
        
        for (AnswerSubmission submission : answers) {
            Long questionId = submission.getQuestionId();
            String userAnswer = submission.getAnswerContent();
            
            QuestionDTO question = questions.get(questionId);
            if (question == null) {
                log.error("Question {} not found in fetched questions", questionId);
                continue;
            }
            
            // Handle empty/null answers
            if (userAnswer == null || userAnswer.trim().isEmpty()) {
                userAnswer = "";
            }
            
            // Get question type
            Long questionTypeId = question.getQuestionTypeId();
            boolean isCorrect = false;
            String correctAnswerDisplay = "";
            
            // Grade based on question type
            if (questionTypeId == null) {
                log.warn("Question {} has no questionTypeId, skipping", questionId);
                continue;
            } else if (questionTypeId == 1) {
                // SingleChoice: fetch answers and check if selected ID is correct
                List<com.abc.exam_service.dto.AnswerDTO> answersList = questionServiceClient.getAnswersByQuestionId(questionId);
                isCorrect = AnswerGrader.gradeSingleChoice(userAnswer, answersList);
                correctAnswerDisplay = answersList.stream()
                        .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                        .map(a -> String.valueOf(a.getId()))
                        .findFirst()
                        .orElse("");
            } else if (questionTypeId == 2) {
                // MultipleChoice: fetch answers and check if all correct IDs selected
                List<com.abc.exam_service.dto.AnswerDTO> answersList = questionServiceClient.getAnswersByQuestionId(questionId);
                isCorrect = AnswerGrader.gradeMultipleChoice(userAnswer, answersList);
                correctAnswerDisplay = answersList.stream()
                        .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                        .map(a -> String.valueOf(a.getId()))
                        .collect(java.util.stream.Collectors.joining(";"));
            } else if (questionTypeId == 3) {
                // Essay: check if length >= 10 characters
                isCorrect = AnswerGrader.gradeEssay(userAnswer);
                correctAnswerDisplay = question.getQuestionAnswer() != null ? question.getQuestionAnswer() : "";
            } else {
                log.warn("Unknown question type {} for question {}", questionTypeId, questionId);
                continue;
            }
            
            if (isCorrect) {
                correctCount++;
            }
            
            // Create UserAnswer entity
            UserAnswer userAnswerEntity = new UserAnswer();
            userAnswerEntity.setExam(exam);
            userAnswerEntity.setQuestionId(questionId);
            userAnswerEntity.setUserId(userId);
            userAnswerEntity.setAnswerContent(userAnswer);
            userAnswerEntity.setIsCorrect(isCorrect);
            userAnswerEntity.setCreatedAt(LocalDateTime.now());
            userAnswerRepository.save(userAnswerEntity);
            
            // Add to grading details
            AnswerGradingDetail detail = AnswerGradingDetail.builder()
                    .questionId(questionId)
                    .userAnswer(userAnswer)
                    .isCorrect(isCorrect)
                    .correctAnswer(correctAnswerDisplay)
                    .build();
            gradingDetails.add(detail);
        }
        
        // Step 4: Calculate score and determine pass/fail
        int totalQuestions = answers.size();
        double score = AnswerGrader.calculateScore(correctCount, totalQuestions);
        
        // Calculate dynamic pass threshold based on exam level
        // Formula: 50×(1+0.10×(levelId−1))
        double passThreshold = PASS_THRESHOLD; // Default 70%
        if (exam.getLevelId() != null) {
            passThreshold = 50.0 * (1 + 0.10 * (exam.getLevelId() - 1));
            log.info("Dynamic pass threshold for levelId {}: {:.2f}%", exam.getLevelId(), passThreshold);
        } else {
            log.warn("Exam {} has no levelId, using default threshold: {:.2f}%", examId, passThreshold);
        }
        
        boolean passStatus = AnswerGrader.determinePassStatus(score, passThreshold);
        
        log.info("Grading complete: {}/{} correct, score: {:.2f}%, pass: {} (threshold: {:.2f}%)", 
                correctCount, totalQuestions, score, passStatus, passThreshold);
        
        // Step 5: Save Result entity
        Result result = new Result();
        result.setExam(exam);
        result.setUserId(userId);
        result.setScore(score);
        result.setPassStatus(passStatus);
        result.setCompletedAt(LocalDateTime.now());
        resultRepository.save(result);
        
        // Step 6: Build and return response
        return ExamGradingResponse.builder()
                .examId(examId)
                .userId(userId)
                .score(score)
                .passStatus(passStatus)
                .totalQuestions(totalQuestions)
                .correctAnswers(correctCount)
                .incorrectAnswers(totalQuestions - correctCount)
                .completedAt(LocalDateTime.now())
                .details(gradingDetails)
                .build();
    }

    /**
     * Retrieves detailed exam history for a user.
     * 
     * @param examId the exam ID
     * @param userId the user ID
     * @return ExamHistoryResponse with question-by-question breakdown
     */
    @Transactional(readOnly = true)
    public ExamHistoryResponse getExamHistory(Long examId, Long userId) {
        log.info("Retrieving exam history for exam {} and user {}", examId, userId);
        
        // Fetch exam
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));
        
        // Fetch top 2 results ordered by completedAt DESC to determine the latest attempt
        List<Result> recentResults = resultRepository.findTop2ByExamIdAndUserIdOrderByCompletedAtDesc(examId, userId);
        
        if (recentResults.isEmpty()) {
            log.info("No results found for exam {} and user {} - user has not submitted", examId, userId);
            return ExamHistoryResponse.builder()
                    .examId(examId)
                    .userId(userId)
                    .examTitle(exam.getTitle())
                    .answers(Collections.emptyList())
                    .build();
        }
        
        // Get the latest result
        Result latestResult = recentResults.get(0);
        java.time.LocalDateTime latestCompletedAt = latestResult.getCompletedAt();
        
        // Fetch user answers for the latest attempt only
        List<UserAnswer> userAnswers;
        if (recentResults.size() > 1) {
            // Has previous attempt - filter between previous and latest completedAt
            Result previousResult = recentResults.get(1);
            java.time.LocalDateTime previousCompletedAt = previousResult.getCompletedAt();
            userAnswers = userAnswerRepository.findByExamIdAndUserIdAndCreatedAtBetween(
                    examId, userId, previousCompletedAt, latestCompletedAt);
            log.info("Filtering answers between {} and {} for latest attempt", previousCompletedAt, latestCompletedAt);
        } else {
            // First attempt - get all answers up to latest completedAt
            userAnswers = userAnswerRepository.findByExamIdAndUserIdAndCreatedAtBefore(
                    examId, userId, latestCompletedAt);
            log.info("Filtering answers up to {} for first attempt", latestCompletedAt);
        }
        
        if (userAnswers.isEmpty()) {
            log.warn("No answers found for latest attempt of exam {} and user {}", examId, userId);
            return ExamHistoryResponse.builder()
                    .examId(examId)
                    .userId(userId)
                    .examTitle(exam.getTitle())
                    .score(latestResult.getScore())
                    .passStatus(latestResult.getPassStatus())
                    .completedAt(latestResult.getCompletedAt())
                    .answers(Collections.emptyList())
                    .build();
        }
        
        // Fetch exam questions to get order numbers
        List<ExamQuestion> examQuestions = examQuestionRepository.findByExamIdOrderByOrderNumberAsc(examId);
        Map<Long, Integer> questionOrderMap = examQuestions.stream()
                .collect(Collectors.toMap(
                        ExamQuestion::getQuestionId, 
                        ExamQuestion::getOrderNumber,
                        (existing, replacement) -> existing  // Keep first occurrence if duplicate
                ));
        
        // Fetch question details from Question Service
        List<Long> questionIds = userAnswers.stream()
                .map(UserAnswer::getQuestionId)
                .collect(Collectors.toList());
        Map<Long, QuestionDTO> questions = questionServiceClient.getQuestionsWithAnswers(questionIds);
        
        // Build answer history items
        List<AnswerHistoryItem> answerItems = new ArrayList<>();
        
        for (UserAnswer userAnswer : userAnswers) {
            Long questionId = userAnswer.getQuestionId();
            QuestionDTO question = questions.get(questionId);
            
            // Determine userAnswer display based on question type
            String userAnswerDisplay = userAnswer.getAnswerContent();
            Long questionTypeId = question != null ? question.getQuestionTypeId() : null;
            
            if ((questionTypeId == 1 || questionTypeId == 2) && userAnswer.getAnswerContent() != null && !userAnswer.getAnswerContent().trim().isEmpty()) {
                try {
                    // Parse selected answer IDs
                    String[] selectedIds = userAnswer.getAnswerContent().split(";");
                    List<com.abc.exam_service.dto.AnswerDTO> answersList = questionServiceClient.getAnswersByQuestionId(questionId);
                    
                    // Map answer IDs to their content
                    java.util.Map<Long, String> answerIdToContent = answersList.stream()
                            .collect(java.util.stream.Collectors.toMap(
                                    com.abc.exam_service.dto.AnswerDTO::getId,
                                    com.abc.exam_service.dto.AnswerDTO::getAnswerContent
                            ));
                    
                    // Get content for selected IDs
                    java.util.List<String> selectedContents = new java.util.ArrayList<>();
                    for (String idStr : selectedIds) {
                        try {
                            Long answerId = Long.parseLong(idStr.trim());
                            String content = answerIdToContent.get(answerId);
                            if (content != null) {
                                selectedContents.add(content);
                            }
                        } catch (NumberFormatException e) {
                            // Skip invalid IDs
                        }
                    }
                    
                    userAnswerDisplay = String.join(";", selectedContents);
                } catch (Exception e) {
                    // If parsing fails, keep original answer content
                    log.warn("Failed to parse user answer for question {}: {}", questionId, userAnswer.getAnswerContent());
                }
            }
            
            AnswerHistoryItem.AnswerHistoryItemBuilder itemBuilder = AnswerHistoryItem.builder()
                    .questionId(questionId)
                    .orderNumber(questionOrderMap.getOrDefault(questionId, 0))
                    .userAnswer(userAnswerDisplay)
                    .isCorrect(userAnswer.getIsCorrect());
            
            // Add question content and metadata if available
            if (question != null) {
                itemBuilder.questionContent(question.getQuestionText());
                
                // Set correctAnswer based on question type
                String correctAnswer = "";
                
                if (questionTypeId == 1 || questionTypeId == 2) {
                    // For SingleChoice and MultipleChoice: get answers and filter correct ones
                    List<com.abc.exam_service.dto.AnswerDTO> answersList = questionServiceClient.getAnswersByQuestionId(questionId);
                    correctAnswer = answersList.stream()
                            .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                            .map(com.abc.exam_service.dto.AnswerDTO::getAnswerContent)
                            .collect(java.util.stream.Collectors.joining(";"));
                } else if (questionTypeId == 3) {
                    // For Essay: use questionAnswer
                    correctAnswer = question.getQuestionAnswer();
                }
                
                itemBuilder.correctAnswer(correctAnswer);
                
                // Build metadata
                QuestionMetadata metadata = QuestionMetadata.builder()
                        .fieldId(question.getFieldId())
                        .topicIds(question.getTopicId() != null ? List.of(question.getTopicId()) : Collections.emptyList())
                        .levelId(question.getLevelId())
                        .questionTypeId(question.getQuestionTypeId())
                        .build();
                itemBuilder.metadata(metadata);
            }
            
            answerItems.add(itemBuilder.build());
        }
        
        // Sort by order number
        answerItems.sort(Comparator.comparing(AnswerHistoryItem::getOrderNumber));
        
        // Build response with latest result data
        return ExamHistoryResponse.builder()
                .examId(examId)
                .userId(userId)
                .examTitle(exam.getTitle())
                .score(latestResult.getScore())
                .passStatus(latestResult.getPassStatus())
                .completedAt(latestResult.getCompletedAt())
                .answers(answerItems)
                .build();
    }

    /**
     * Retrieves the most recent exam result for a user.
     * 
     * @param examId the exam ID
     * @param userId the user ID
     * @return ResultResponse with score, pass status, and completion time
     */
    @Transactional(readOnly = true)
    public ResultResponse getExamResult(Long examId, Long userId) {
        log.info("Retrieving exam result for exam {} and user {}", examId, userId);
        
        Result result = resultRepository.findTopByExamIdAndUserIdOrderByCompletedAtDesc(examId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No result found for exam " + examId + " and user " + userId));
        
        return ResultResponse.builder()
                .id(result.getId())
                .examId(examId)
                .userId(userId)
                .score(result.getScore())
                .passStatus(result.getPassStatus())
                .feedback(result.getFeedback())
                .completedAt(result.getCompletedAt())
                .build();
    }
}
