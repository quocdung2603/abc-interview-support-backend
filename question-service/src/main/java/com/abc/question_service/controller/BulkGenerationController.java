package com.abc.question_service.controller;

import com.abc.question_service.dto.GenerationReport;
import com.abc.question_service.dto.GenerationRequest;
import com.abc.question_service.service.QuestionGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bulk Generation", description = "APIs for bulk question generation")
public class BulkGenerationController {

    private final QuestionGeneratorService generatorService;

    @PostMapping("/bulk-generate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Generate questions in bulk",
        description = "Generates a large number of unique interview questions distributed across all field-topic-level-type combinations. Requires ADMIN role."
    )
    public ResponseEntity<GenerationReport> generateQuestions(
            @Valid @RequestBody GenerationRequest request) {
        
        log.info("Received bulk generation request for {} questions", request.getTargetCount());
        
        try {
            GenerationReport report = generatorService.generateQuestions(request);
            
            if (report.getGeneratedCount() > 0) {
                log.info("Successfully generated {} questions", report.getGeneratedCount());
                return ResponseEntity.ok(report);
            } else {
                log.warn("No questions generated. Errors: {}", report.getErrors());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(report);
            }
            
        } catch (Exception e) {
            log.error("Error during bulk generation: {}", e.getMessage(), e);
            GenerationReport errorReport = new GenerationReport();
            errorReport.setRequestedCount(request.getTargetCount());
            errorReport.setGeneratedCount(0);
            errorReport.setFailedCount(request.getTargetCount());
            errorReport.addError("Generation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorReport);
        }
    }
}
