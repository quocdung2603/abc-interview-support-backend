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
     * 
     * @param examId the exam ID
     * @param userId the user ID
     * @throws InvalidRequestException if user is not registered
     */
    private void validateRegistration(Long examId, Long userId) {
        boolean isRegistered = examRegistrationRepository
                .existsByExamIdAndUserIdAndRegistrationStatus(examId, userId, "REGISTERED");
        
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
        validateRegistration(examId, userId);
        
        List<Long> submittedQuestionIds = answers.stream()
                .map(AnswerSubmission::getQuestionId)
                .collect(Collectors.toList());
        validateQuestionIds(examId, submittedQuestionIds);
        
        // Step 2: Fetch correct answers from Question Service
        log.info("Fetching correct answers for {} questions", submittedQuestionIds.size());
        Map<Long, QuestionDTO> questions = questionServiceClient.getQuestionsWithAnswers(submittedQuestionIds);
        
        if (questions.size() < submittedQuestionIds.size()) {
            log.warn("Could not fetch all questions. Expected {}, got {}", 
                    submittedQuestionIds.size(), questions.size());
        }
        
        // Step 3: Grade each answer
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
            
            String correctAnswer = question.getQuestionAnswer();
            
            // Handle empty/null answers
            if (userAnswer == null || userAnswer.trim().isEmpty()) {
                userAnswer = "";
            }
            
            // Grade the answer
            boolean isCorrect = AnswerGrader.isCorrect(userAnswer, correctAnswer);
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
                    .correctAnswer(correctAnswer)
                    .build();
            gradingDetails.add(detail);
        }
        
        // Step 4: Calculate score and determine pass/fail
        int totalQuestions = answers.size();
        double score = AnswerGrader.calculateScore(correctCount, totalQuestions);
        boolean passStatus = AnswerGrader.determinePassStatus(score, PASS_THRESHOLD);
        
        log.info("Grading complete: {}/{} correct, score: {:.2f}%, pass: {}", 
                correctCount, totalQuestions, score, passStatus);
        
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
        
        // Fetch user answers ordered by creation time
        List<UserAnswer> userAnswers = userAnswerRepository.findByExamIdAndUserIdOrderByCreatedAtAsc(examId, userId);
        
        if (userAnswers.isEmpty()) {
            log.info("No answers found for exam {} and user {}", examId, userId);
            return ExamHistoryResponse.builder()
                    .examId(examId)
                    .userId(userId)
                    .examTitle(exam.getTitle())
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
            
            AnswerHistoryItem.AnswerHistoryItemBuilder itemBuilder = AnswerHistoryItem.builder()
                    .questionId(questionId)
                    .orderNumber(questionOrderMap.getOrDefault(questionId, 0))
                    .userAnswer(userAnswer.getAnswerContent())
                    .isCorrect(userAnswer.getIsCorrect());
            
            // Add question content and metadata if available
            if (question != null) {
                itemBuilder.questionContent(question.getQuestionText())
                        .correctAnswer(question.getQuestionAnswer());
                
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
        
        // Fetch result if exists
        Optional<Result> resultOpt = resultRepository.findTopByExamIdAndUserIdOrderByCompletedAtDesc(examId, userId);
        
        ExamHistoryResponse.ExamHistoryResponseBuilder responseBuilder = ExamHistoryResponse.builder()
                .examId(examId)
                .userId(userId)
                .examTitle(exam.getTitle())
                .answers(answerItems);
        
        resultOpt.ifPresent(result -> {
            responseBuilder.score(result.getScore())
                    .passStatus(result.getPassStatus())
                    .completedAt(result.getCompletedAt());
        });
        
        return responseBuilder.build();
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
