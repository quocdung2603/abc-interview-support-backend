package com.abc.exam_service.service;

import com.abc.exam_service.dto.*;
import com.abc.exam_service.entity.*;
import com.abc.exam_service.mapper.Mappers;
import com.abc.exam_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExamService {
    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ResultRepository resultRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final ExamRegistrationRepository examRegistrationRepository;
    private final Mappers mappers;

    public ExamResponse createExam(ExamRequest req) {
        Exam exam = mappers.toEntity(req);
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

    public ExamQuestionResponse addQuestionToExam(ExamQuestionRequest req) {
        return mappers.toResponse(examQuestionRepository.save(mappers.toEntity(req)));
    }

    public void removeQuestionsFromExam(Long examId) {
        examQuestionRepository.deleteByExamId(examId);
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

    public ExamRegistrationResponse registerForExam(ExamRegistrationRequest req) {
        if (examRegistrationRepository.existsByExamIdAndUserId(req.getExamId(), req.getUserId())) {
            throw new RuntimeException("Already registered for this exam");
        }
        ExamRegistration registration = mappers.toEntity(req);
        registration.setRegistrationStatus("REGISTERED");
        registration.setRegisteredAt(LocalDateTime.now());
        return mappers.toResponse(examRegistrationRepository.save(registration));
    }

    public ExamRegistrationResponse cancelRegistration(Long registrationId) {
        ExamRegistration registration = examRegistrationRepository.findById(registrationId).orElseThrow();
        registration.setRegistrationStatus("CANCELLED");
        return mappers.toResponse(examRegistrationRepository.save(registration));
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
        return mappers.toResponse(examRepository.findById(id).orElseThrow());
    }

    public ExamResponse updateExam(Long id, ExamRequest req) {
        Exam exam = examRepository.findById(id).orElseThrow();
        exam.setTitle(req.getTitle());
        exam.setPosition(req.getPosition());
        exam.setTopics(req.getTopics() != null ? req.getTopics().toString() : null);
        exam.setQuestionTypes(req.getQuestionTypes() != null ? req.getQuestionTypes().toString() : null);
        exam.setQuestionCount(req.getQuestionCount());
        exam.setDuration(req.getDuration());
        exam.setLanguage(req.getLanguage());
        return mappers.toResponse(examRepository.save(exam));
    }

    public void deleteExam(Long id) {
        examRepository.deleteById(id);
    }

    public ResultResponse getResultById(Long id) {
        return mappers.toResponse(resultRepository.findById(id).orElseThrow());
    }

    public UserAnswerResponse getUserAnswerById(Long id) {
        return mappers.toResponse(userAnswerRepository.findById(id).orElseThrow());
    }

    public ExamRegistrationResponse getRegistrationById(Long id) {
        return mappers.toResponse(examRegistrationRepository.findById(id).orElseThrow());
    }
}
