package com.abc.exam_service.controller;

import com.abc.exam_service.dto.*;
import com.abc.exam_service.service.GradingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
public class GradingController {
    
    private final GradingService gradingService;
    
    /**
     * Submit and grade an exam.
     * 
     * POST /exams/{examId}/submit
     */
    @PostMapping("/{examId}/submit")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ExamGradingResponse submitExam(
            @PathVariable Long examId,
            @Valid @RequestBody SubmitExamRequest request) {
        return gradingService.submitAndGradeExam(examId, request.getUserId(), request.getAnswers());
    }
    
    /**
     * Get detailed exam history for a user.
     * 
     * GET /exams/{examId}/history?userId={userId}
     */
    @GetMapping("/{examId}/history")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ExamHistoryResponse getExamHistory(
            @PathVariable Long examId,
            @RequestParam Long userId) {
        return gradingService.getExamHistory(examId, userId);
    }
    
    /**
     * Get exam result for a user.
     * 
     * GET /exams/{examId}/results/{userId}
     */
    @GetMapping("/{examId}/results/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('RECRUITER')")
    public ResultResponse getExamResult(
            @PathVariable Long examId,
            @PathVariable Long userId) {
        return gradingService.getExamResult(examId, userId);
    }
}
