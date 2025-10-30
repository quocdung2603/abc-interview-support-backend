package com.abc.exam_service.controller;

import com.abc.exam_service.dto.*;
import com.abc.exam_service.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
public class ExamController {
    private final ExamService examService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('RECRUITER')")
    public ExamResponse createExam(@RequestBody ExamRequest req) {
        return examService.createExam(req);
    }

    @PostMapping("/{examId}/publish")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECRUITER')")
    public ExamResponse publishExam(@PathVariable Long examId, @RequestParam Long userId) {
        return examService.publishExam(examId, userId);
    }

    @PostMapping("/{examId}/start")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ExamResponse startExam(@PathVariable Long examId) {
        return examService.startExam(examId);
    }

    @PostMapping("/{examId}/complete")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ExamResponse completeExam(@PathVariable Long examId) {
        return examService.completeExam(examId);
    }

    @GetMapping
    public Page<ExamResponse> getAllExams(Pageable pageable) {
        return examService.getAllExams(pageable);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Page<ExamResponse> listExamsByUser(@PathVariable Long userId, Pageable pageable) {
        return examService.listExamsByUser(userId, pageable);
    }

    @GetMapping("/type")
    public Page<ExamResponse> listExamsByType(@RequestParam String type, Pageable pageable) {
        return examService.listExamsByType(type, pageable);
    }

    @PostMapping("/questions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECRUITER')")
    public ExamQuestionResponse addQuestionToExam(@RequestBody ExamQuestionRequest req) {
        return examService.addQuestionToExam(req);
    }

    @DeleteMapping("/{examId}/questions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECRUITER')")
    public void removeQuestionsFromExam(@PathVariable Long examId) {
        examService.removeQuestionsFromExam(examId);
    }

    @PostMapping("/results")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResultResponse submitResult(@jakarta.validation.Valid @RequestBody ResultRequest req) {
        return examService.submitResult(req);
    }

    @PostMapping("/answers")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public UserAnswerResponse submitAnswer(@jakarta.validation.Valid @RequestBody UserAnswerRequest req) {
        return examService.submitAnswer(req);
    }

    @GetMapping("/{examId}/answers/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Page<UserAnswerResponse> getUserAnswers(@PathVariable Long examId, @PathVariable Long userId, Pageable pageable) {
        return examService.getUserAnswers(examId, userId, pageable);
    }

    @PostMapping("/registrations")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ExamRegistrationResponse registerForExam(@RequestBody ExamRegistrationRequest req) {
        return examService.registerForExam(req);
    }

    @PostMapping("/registrations/{registrationId}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ExamRegistrationResponse cancelRegistration(@PathVariable Long registrationId) {
        return examService.cancelRegistration(registrationId);
    }

    @GetMapping("/{examId}/registrations")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECRUITER')")
    public Page<ExamRegistrationResponse> listRegistrationsByExam(@PathVariable Long examId, Pageable pageable) {
        return examService.listRegistrationsByExam(examId, pageable);
    }

    @GetMapping("/registrations/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Page<ExamRegistrationResponse> listRegistrationsByUser(@PathVariable Long userId, Pageable pageable) {
        return examService.listRegistrationsByUser(userId, pageable);
    }

    @GetMapping("/{examId}/results")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECRUITER')")
    public Page<ResultResponse> listResultsByExam(@PathVariable Long examId, Pageable pageable) {
        return examService.listResultsByExam(examId, pageable);
    }

    @GetMapping("/results/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Page<ResultResponse> listResultsByUser(@PathVariable Long userId, Pageable pageable) {
        return examService.listResultsByUser(userId, pageable);
    }

    // Additional CRUD endpoints



    @GetMapping("/{id:[0-9]+}")
    public ExamResponse getExamById(@PathVariable Long id) {
        return examService.getExamById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECRUITER')")
    public ExamResponse updateExam(@PathVariable Long id, @RequestBody ExamRequest req) {
        return examService.updateExam(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECRUITER')")
    public void deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
    }

    @GetMapping("/results/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResultResponse getResultById(@PathVariable Long id) {
        return examService.getResultById(id);
    }

    @GetMapping("/answers/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public UserAnswerResponse getUserAnswerById(@PathVariable Long id) {
        return examService.getUserAnswerById(id);
    }

    @GetMapping("/registrations/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ExamRegistrationResponse getRegistrationById(@PathVariable Long id) {
        return examService.getRegistrationById(id);
    }

    @GetMapping("/types")
    public java.util.List<String> getExamTypes() {
        return java.util.Arrays.asList("VIRTUAL", "RECRUITER");
    }
}
