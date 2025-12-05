package com.abc.exam_service.service;

import com.abc.exam_service.dto.*;
import com.abc.exam_service.entity.*;
import com.abc.exam_service.mapper.Mappers;
import com.abc.exam_service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamService {
    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ResultRepository resultRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final ExamRegistrationRepository examRegistrationRepository;
    private final QuestionServiceClient questionServiceClient;
    private final Mappers mappers;
    private final jakarta.persistence.EntityManager entityManager;

    @Transactional
    public ExamResponse createExam(ExamRequest req) {
        // Validate that referenced IDs exist (optional - skip validation if question-service unavailable)
        // TODO: Re-enable validation when question-service is stable
        /*
        if (req.getFieldId() != null && !questionServiceClient.fieldExists(req.getFieldId())) {
            throw new RuntimeException("Field not found with id: " + req.getFieldId());
        }
        if (req.getTopicIds() != null && !req.getTopicIds().isEmpty()) {
            for (Long topicId : req.getTopicIds()) {
                if (!questionServiceClient.topicExists(topicId)) {
                    throw new RuntimeException("Topic not found with id: " + topicId);
                }
            }
        }
        if (req.getLevelId() != null && !questionServiceClient.levelExists(req.getLevelId())) {
            throw new RuntimeException("Level not found with id: " + req.getLevelId());
        }
        */
        
        Exam exam = mappers.toEntity(req);
        
        // Set numeric ID fields
        exam.setFieldId(req.getFieldId());
        exam.setLevelId(req.getLevelId());
        // topicIds and questionTypeIds are already set by mapper
        
        exam.setStatus("DRAFT");
        exam.setCreatedAt(LocalDateTime.now());
        exam.setCreatedBy(req.getUserId());
        return mappers.toResponse(examRepository.save(exam));
    }

    public ExamResponse publishExam(Long examId, Long userId) {
        Exam exam = examRepository.findById(examId).orElseThrow();
        exam.setStatus("PUBLISHED");
        return mappers.toResponse(examRepository.save(exam));
    }

    public ExamResponse startExam(Long examId) {
        Exam exam = examRepository.findById(examId).orElseThrow();
        exam.setStatus("ONGOING");
        return mappers.toResponse(examRepository.save(exam));
    }

    public ExamResponse completeExam(Long examId) {
        Exam exam = examRepository.findById(examId).orElseThrow();
        exam.setStatus("COMPLETED");
        return mappers.toResponse(examRepository.save(exam));
    }

    public Page<ExamResponse> getAllExams(Pageable pageable) {
        return examRepository.findAll(pageable).map(mappers::toResponse);
    }

    public Page<ExamResponse> listExamsByUser(Long userId, Pageable pageable) {
        return examRepository.findByUserId(userId, pageable).map(mappers::toResponse);
    }

    public Page<ExamResponse> listExamsByType(String examType, Pageable pageable) {
        return examRepository.findByExamType(examType, pageable).map(mappers::toResponse);
    }

    @Transactional
    public ExamQuestionResponse addQuestionToExam(ExamQuestionRequest req) {
        ExamQuestion examQuestion = mappers.toEntity(req);
        // Set exam entity for proper foreign key relationship
        Exam exam = examRepository.findById(req.getExamId()).orElseThrow();
        examQuestion.setExam(exam);
        ExamQuestion saved = examQuestionRepository.save(examQuestion);
        // Force load exam to avoid LazyInitializationException when mapping
        if (saved.getExam() != null) {
            saved.getExam().getId();
        }
        return mappers.toResponse(saved);
    }

    @Transactional
    public DeleteResponse removeQuestionsFromExam(Long examId) {
        // Verify exam exists before attempting deletion
        if (!examRepository.existsById(examId)) {
            throw new RuntimeException("Exam not found with id: " + examId);
        }
        
        // Count questions before deletion
        long questionCount = examQuestionRepository.countByExamId(examId);
        
        // Delete all exam questions within transaction
        examQuestionRepository.deleteByExamId(examId);
        entityManager.flush();
        
        return DeleteResponse.builder()
                .success(true)
                .message("Successfully removed " + questionCount + " question(s) from exam")
                .id(examId)
                .build();
    }

    public ResultResponse submitResult(ResultRequest req) {
        Result result = mappers.toEntity(req);
        // Link to Exam entity to enable queries like findByExamId
        Exam exam = examRepository.findById(req.getExamId()).orElseThrow();
        result.setExam(exam);
        result.setCompletedAt(LocalDateTime.now());
        return mappers.toResponse(resultRepository.save(result));
    }

    public UserAnswerResponse submitAnswer(UserAnswerRequest req) {
        UserAnswer answer = mappers.toEntity(req);
        // Link to Exam entity to enable queries like findByExamIdAndUserId
        Exam exam = examRepository.findById(req.getExamId()).orElseThrow();
        answer.setExam(exam);
        answer.setCreatedAt(LocalDateTime.now());
        return mappers.toResponse(userAnswerRepository.save(answer));
    }

    public Page<UserAnswerResponse> getUserAnswers(Long examId, Long userId, Pageable pageable) {
        return userAnswerRepository.findByExamIdAndUserId(examId, userId, pageable).map(mappers::toResponse);
    }

    @Transactional
    public ExamRegistrationResponse registerForExam(ExamRegistrationRequest req) {
        if (examRegistrationRepository.existsByExamIdAndUserId(req.getExamId(), req.getUserId())) {
            throw new RuntimeException("Already registered for this exam");
        }
        
        // Fetch the exam entity to establish the relationship
        Exam exam = examRepository.findById(req.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found with id: " + req.getExamId()));
        
        ExamRegistration registration = mappers.toEntity(req);
        registration.setExam(exam); // Set the exam relationship
        registration.setRegistrationStatus("REGISTERED");
        registration.setRegisteredAt(LocalDateTime.now());
        
        ExamRegistration saved = examRegistrationRepository.save(registration);
        
        // Force load exam to avoid LazyInitializationException when mapping
        if (saved.getExam() != null) {
            saved.getExam().getId();
        }
        
        return mappers.toResponse(saved);
    }

    @Transactional
    public ExamRegistrationResponse cancelRegistration(Long registrationId) {
        ExamRegistration registration = examRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found with id: " + registrationId));
        
        registration.setRegistrationStatus("CANCELLED");
        ExamRegistration saved = examRegistrationRepository.save(registration);
        
        // Force load exam to avoid LazyInitializationException when mapping
        if (saved.getExam() != null) {
            saved.getExam().getId();
        }
        
        return mappers.toResponse(saved);
    }

    public Page<ExamRegistrationResponse> listRegistrationsByExam(Long examId, Pageable pageable) {
        return examRegistrationRepository.findByExamId(examId, pageable).map(mappers::toResponse);
    }

    public Page<ExamRegistrationResponse> listRegistrationsByUser(Long userId, Pageable pageable) {
        return examRegistrationRepository.findByUserId(userId, pageable).map(mappers::toResponse);
    }

    public Page<ResultResponse> listResultsByExam(Long examId, Pageable pageable) {
        return resultRepository.findByExamId(examId, pageable).map(mappers::toResponse);
    }

    public Page<ResultResponse> listResultsByUser(Long userId, Pageable pageable) {
        return resultRepository.findByUserId(userId, pageable).map(mappers::toResponse);
    }

    // Additional CRUD methods
    public ExamResponse getExamById(Long id) {
        Exam exam = examRepository.findById(id).orElseThrow();
        ExamResponse response = mappers.toResponse(exam);
        
        // Lấy danh sách câu hỏi theo thứ tự
        List<ExamQuestion> examQuestions = examQuestionRepository.findByExamIdOrderByOrderNumberAsc(id);
        if (!examQuestions.isEmpty()) {
            List<Long> questionIds = examQuestions.stream()
                    .map(ExamQuestion::getQuestionId)
                    .toList();
            
            // Fetch full question details từ question-service
            List<QuestionDTO> questions = new ArrayList<>();
            for (Long questionId : questionIds) {
                try {
                    QuestionDTO question = questionServiceClient.getQuestionById(questionId);
                    questions.add(question);
                } catch (Exception e) {
                    log.warn("Could not fetch question {}: {}", questionId, e.getMessage());
                }
            }
            response.setQuestions(questions);
        }
        
        return response;
    }

    @Transactional
    public ExamResponse updateExam(Long id, ExamRequest req) {
        // Validate that referenced IDs exist (optional - skip validation if question-service unavailable)
        // TODO: Re-enable validation when question-service is stable
        /*
        if (req.getFieldId() != null && !questionServiceClient.fieldExists(req.getFieldId())) {
            throw new RuntimeException("Field not found with id: " + req.getFieldId());
        }
        if (req.getTopicIds() != null && !req.getTopicIds().isEmpty()) {
            for (Long topicId : req.getTopicIds()) {
                if (!questionServiceClient.topicExists(topicId)) {
                    throw new RuntimeException("Topic not found with id: " + topicId);
                }
            }
        }
        if (req.getLevelId() != null && !questionServiceClient.levelExists(req.getLevelId())) {
            throw new RuntimeException("Level not found with id: " + req.getLevelId());
        }
        */
        
        Exam exam = examRepository.findById(id).orElseThrow();
        exam.setTitle(req.getTitle());
        exam.setPosition(req.getPosition());
        exam.setExamType(req.getExamType());
        
        // Update numeric ID fields
        exam.setFieldId(req.getFieldId());
        exam.setLevelId(req.getLevelId());
        
        // Update topicIds and questionTypeIds using mapper
        try {
            if (req.getTopicIds() != null) {
                exam.setTopicIds(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(req.getTopicIds()));
            }
            if (req.getQuestionTypeIds() != null) {
                exam.setQuestionTypeIds(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(req.getQuestionTypeIds()));
            }
        } catch (Exception e) {
            log.warn("Failed to serialize IDs", e);
        }
        
        exam.setQuestionCount(req.getQuestionCount());
        exam.setDuration(req.getDuration());
        exam.setLanguage(req.getLanguage());
        return mappers.toResponse(examRepository.save(exam));
    }

    @Transactional
    public DeleteResponse deleteExam(Long id) {
        // Verify exam exists
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found with id: " + id));
        
        String examTitle = exam.getTitle();
        
        log.info("Deleting exam {} with title: {}", id, examTitle);
        
        // Delete related data first - must flush after each delete to ensure FK constraints are satisfied
        // Order matters: delete child records before parent
        
        // 1. Delete exam questions (child of exam)
        log.info("Deleting exam questions for exam {}", id);
        examQuestionRepository.deleteByExamId(id);
        entityManager.flush();
        entityManager.clear(); // Clear persistence context to ensure delete is committed
        
        // 2. Delete user answers (child of exam)
        log.info("Deleting user answers for exam {}", id);
        userAnswerRepository.deleteByExamId(id);
        entityManager.flush();
        entityManager.clear();
        
        // 3. Delete results (child of exam)
        log.info("Deleting results for exam {}", id);
        resultRepository.deleteByExamId(id);
        entityManager.flush();
        entityManager.clear();
        
        // 4. Delete registrations (child of exam)
        log.info("Deleting registrations for exam {}", id);
        examRegistrationRepository.deleteByExamId(id);
        entityManager.flush();
        entityManager.clear();
        
        // 5. Finally delete exam (parent)
        log.info("Deleting exam {}", id);
        examRepository.deleteById(id);
        entityManager.flush();
        
        log.info("Successfully deleted exam {} with title: {}", id, examTitle);
        
        return DeleteResponse.builder()
                .success(true)
                .message("Successfully deleted exam: " + examTitle)
                .id(id)
                .build();
    }

    public ResultResponse getResultById(Long id) {
        return mappers.toResponse(resultRepository.findById(id).orElseThrow());
    }

    public UserAnswerResponse getUserAnswerById(Long id) {
        return mappers.toResponse(userAnswerRepository.findById(id).orElseThrow());
    }

    @Transactional(readOnly = true)
    public ExamRegistrationResponse getRegistrationById(Long id) {
        ExamRegistration registration = examRegistrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registration not found with id: " + id));
        
        // Force load exam to avoid LazyInitializationException when mapping
        if (registration.getExam() != null) {
            registration.getExam().getId();
        }
        
        return mappers.toResponse(registration);
    }

    @Transactional
    public RandomQuestionsResponse addRandomQuestionsToExam(RandomQuestionsRequest req) {
        try {
            // Verify exam exists
            Exam exam = examRepository.findById(req.getExamId())
                    .orElseThrow(() -> new RuntimeException("Exam not found with id: " + req.getExamId()));
            
            log.info("Fetching random questions for exam {} with criteria: fieldId={}, topicIds={}, levelId={}, typeId={}, count={}", 
                    req.getExamId(), req.getFieldId(), req.getTopicIds(), req.getLevelId(), req.getQuestionTypeId(), req.getNumberOfQuestions());
            
            // Fetch questions from question-service using numeric IDs
            List<QuestionDTO> questions = questionServiceClient.searchQuestionsByIds(
                    req.getFieldId(),
                    req.getTopicIds(),
                    req.getLevelId(),
                    req.getQuestionTypeId(),
                    req.getNumberOfQuestions() * 2 // Fetch more to have options
            );
            
            if (questions == null || questions.isEmpty()) {
                throw new RuntimeException("No questions found matching the criteria");
            }
            
            // Shuffle and limit to requested number - create mutable list first!
            List<QuestionDTO> mutableQuestions = new ArrayList<>(questions);
            Collections.shuffle(mutableQuestions);
            List<QuestionDTO> selectedQuestions = mutableQuestions.stream()
                    .limit(req.getNumberOfQuestions())
                    .collect(Collectors.toList());
            
            log.info("Selected {} random questions from {} available", selectedQuestions.size(), questions.size());
            
            // Get current max order number for this exam
            Integer maxOrder = examQuestionRepository.findMaxOrderNumberByExamId(req.getExamId());
            int startOrder = (maxOrder != null ? maxOrder : 0) + 1;
            
            log.info("Starting order number: {}", startOrder);
            
            // Add questions to exam
            List<Long> addedQuestionIds = new ArrayList<>();
            for (int i = 0; i < selectedQuestions.size(); i++) {
                QuestionDTO question = selectedQuestions.get(i);
                
                ExamQuestion examQuestion = new ExamQuestion();
                examQuestion.setExam(exam);
                examQuestion.setQuestionId(question.getId());
                examQuestion.setOrderNumber(startOrder + i);
                
                log.info("Saving exam question: examId={}, questionId={}, orderNumber={}", 
                        exam.getId(), question.getId(), examQuestion.getOrderNumber());
                
                ExamQuestion saved = examQuestionRepository.save(examQuestion);
                log.info("Saved exam question with id: {}", saved.getId());
                addedQuestionIds.add(question.getId());
            }
            
            log.info("Successfully added {} questions to exam {}", addedQuestionIds.size(), req.getExamId());
            
            RandomQuestionsResponse response = new RandomQuestionsResponse();
            response.setExamId(req.getExamId());
            response.setAddedCount(addedQuestionIds.size());
            response.setQuestionIds(addedQuestionIds);
            
            return response;
        } catch (Exception e) {
            log.error("Error adding random questions to exam", e);
            throw new RuntimeException("Failed to add random questions: " + e.getMessage(), e);
        }
    }
    
    @Transactional
    public CreateExamWithQuestionsResponse createExamWithRandomQuestions(CreateExamWithQuestionsRequest req, Long userId) {
        try {
            log.info("Creating exam with random questions for user {}: title={}, fieldId={}, topicIds={}, levelId={}, typeId={}, count={}", 
                    userId, req.getTitle(), req.getFieldId(), req.getTopicIds(), req.getLevelId(), req.getQuestionTypeId(), req.getNumberOfQuestions());
            
            // 1. Fetch random questions first using numeric IDs
            List<QuestionDTO> questions = questionServiceClient.searchQuestionsByIds(
                    req.getFieldId(),
                    req.getTopicIds(),
                    req.getLevelId(),
                    req.getQuestionTypeId(),
                    req.getNumberOfQuestions() * 2 // Fetch more to have options
            );
            
            if (questions == null || questions.isEmpty()) {
                throw new RuntimeException("No questions found matching the criteria");
            }
            
            // Shuffle and limit
            List<QuestionDTO> mutableQuestions = new ArrayList<>(questions);
            Collections.shuffle(mutableQuestions);
            List<QuestionDTO> selectedQuestions = mutableQuestions.stream()
                    .limit(req.getNumberOfQuestions())
                    .collect(Collectors.toList());
            
            log.info("Selected {} random questions from {} available", selectedQuestions.size(), questions.size());
            
            // 2. Create exam - PRACTICE exams are auto-published
            Exam exam = new Exam();
            exam.setUserId(userId);
            exam.setTitle(req.getTitle());
            exam.setPosition(req.getPosition());
            exam.setDuration(req.getDuration());
            exam.setLanguage(req.getLanguage());
            exam.setQuestionCount(selectedQuestions.size());
            exam.setExamType("PRACTICE"); // User self-practice
            exam.setStatus("PUBLISHED"); // PRACTICE exams are auto-published
            exam.setCreatedAt(LocalDateTime.now());
            exam.setCreatedBy(userId);
            
            // Store numeric IDs to avoid Unicode encoding issues
            exam.setFieldId(req.getFieldId());
            exam.setLevelId(req.getLevelId());
            
            // Store topicIds and questionTypeIds as JSON strings
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                if (req.getTopicIds() != null && !req.getTopicIds().isEmpty()) {
                    exam.setTopicIds(mapper.writeValueAsString(req.getTopicIds()));
                }
                if (req.getQuestionTypeId() != null) {
                    exam.setQuestionTypeIds(mapper.writeValueAsString(List.of(req.getQuestionTypeId())));
                }
            } catch (Exception e) {
                log.warn("Failed to serialize IDs", e);
            }
            
            Exam savedExam = examRepository.save(exam);
            log.info("Created exam with id: {} (fieldId={}, topicIds={}, levelId={})", 
                    savedExam.getId(), savedExam.getFieldId(), savedExam.getTopicIds(), savedExam.getLevelId());
            
            // 3. Add questions to exam
            List<Long> addedQuestionIds = new ArrayList<>();
            for (int i = 0; i < selectedQuestions.size(); i++) {
                QuestionDTO question = selectedQuestions.get(i);
                
                ExamQuestion examQuestion = new ExamQuestion();
                examQuestion.setExam(savedExam);
                examQuestion.setQuestionId(question.getId());
                examQuestion.setOrderNumber(i + 1);
                
                examQuestionRepository.save(examQuestion);
                addedQuestionIds.add(question.getId());
            }
            
            log.info("Successfully created exam {} with {} questions", savedExam.getId(), addedQuestionIds.size());
            
            // 4. Build response with full question details
            CreateExamWithQuestionsResponse response = new CreateExamWithQuestionsResponse();
            response.setExamId(savedExam.getId());
            response.setTitle(savedExam.getTitle());
            response.setStatus(savedExam.getStatus());
            response.setDuration(savedExam.getDuration());
            response.setQuestionCount(savedExam.getQuestionCount());
            response.setQuestionIds(addedQuestionIds);
            response.setQuestions(selectedQuestions); // Trả về danh sách câu hỏi đầy đủ kèm đáp án
            
            return response;
        } catch (Exception e) {
            log.error("Error creating exam with random questions", e);
            throw new RuntimeException("Failed to create exam with random questions: " + e.getMessage(), e);
        }
    }
}
