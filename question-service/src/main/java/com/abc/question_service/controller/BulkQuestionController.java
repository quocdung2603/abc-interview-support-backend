package com.abc.question_service.controller;

import com.abc.question_service.dto.BulkGenerationRequest;
import com.abc.question_service.dto.GenerationProgress;
import com.abc.question_service.dto.GenerationResult;
import com.abc.question_service.dto.InitializationResult;
import com.abc.question_service.service.BulkGenerationOrchestrator;
import com.abc.question_service.service.DatabaseInitializationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for bulk question generation operations.
 * Provides endpoints for database initialization and bulk generation.
 */
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bulk Question Generation", description = "APIs for bulk question generation and database initialization")
public class BulkQuestionController {
    
    private final DatabaseInitializationService initializationService;
    private final BulkGenerationOrchestrator generationOrchestrator;
    
    /**
     * Resets the database by deleting all questions and answers.
     * Preserves reference data (fields, topics, levels, question types).
     */
    @PostMapping("/reset-database")
    @Operation(summary = "Reset database", 
               description = "Deletes all questions and answers while preserving reference data")
    public ResponseEntity<InitializationResult> resetDatabase() {
        log.info("Received request to reset database");
        
        try {
            InitializationResult result = initializationService.resetDatabase();
            
            if (result.getSuccess()) {
                log.info("Database reset successful: {} questions, {} answers deleted",
                        result.getQuestionsDeleted(), result.getAnswersDeleted());
                return ResponseEntity.ok(result);
            } else {
                log.error("Database reset failed: {}", result.getErrors());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }
            
        } catch (Exception e) {
            log.error("Error during database reset", e);
            InitializationResult errorResult = InitializationResult.builder()
                    .success(false)
                    .message("Database reset failed: " + e.getMessage())
                    .build();
            errorResult.getErrors().add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }
    
    /**
     * Initializes reference data in the database.
     * Creates fields, topics, levels, and question types if they don't exist.
     */
    @PostMapping("/initialize-reference-data")
    @Operation(summary = "Initialize reference data",
               description = "Creates or verifies all reference data (fields, topics, levels, question types)")
    public ResponseEntity<InitializationResult> initializeReferenceData() {
        log.info("Received request to initialize reference data");
        
        try {
            InitializationResult result = initializationService.initializeReferenceData();
            
            if (result.getSuccess()) {
                log.info("Reference data initialization successful: {} fields, {} topics, {} levels, {} question types",
                        result.getFieldsCreated(), result.getTopicsCreated(),
                        result.getLevelsCreated(), result.getQuestionTypesCreated());
                return ResponseEntity.ok(result);
            } else {
                log.error("Reference data initialization failed: {}", result.getErrors());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }
            
        } catch (Exception e) {
            log.error("Error during reference data initialization", e);
            InitializationResult errorResult = InitializationResult.builder()
                    .success(false)
                    .message("Reference data initialization failed: " + e.getMessage())
                    .build();
            errorResult.getErrors().add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }
    
    /**
     * Generates questions in bulk according to the request parameters.
     */
    @PostMapping("/bulk-generate")
    @Operation(summary = "Generate questions in bulk",
               description = "Generates a specified number of unique questions distributed across all metadata combinations")
    public ResponseEntity<?> bulkGenerate(
            @Valid @RequestBody BulkGenerationRequest request,
            BindingResult bindingResult) {
        
        log.info("Received bulk generation request: {} questions, batch size {}",
                request.getTargetCount(), request.getBatchSize());
        
        // Validate request
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            
            log.warn("Invalid bulk generation request: {}", errors);
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Validation failed", "details", errors)
            );
        }
        
        try {
            GenerationResult result = generationOrchestrator.generateQuestions(request);
            
            if (result.getSuccess()) {
                log.info("Bulk generation completed: {} questions generated, {} failed",
                        result.getGeneratedCount(), result.getFailedCount());
                return ResponseEntity.ok(result);
            } else {
                log.error("Bulk generation failed: {}", result.getErrors());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }
            
        } catch (Exception e) {
            log.error("Error during bulk generation", e);
            GenerationResult errorResult = GenerationResult.builder()
                    .success(false)
                    .requestedCount(request.getTargetCount())
                    .generatedCount(0)
                    .failedCount(request.getTargetCount())
                    .build();
            errorResult.getErrors().add("Generation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }
    
    /**
     * Gets the current progress of a generation job.
     */
    @GetMapping("/generation-progress/{jobId}")
    @Operation(summary = "Get generation progress",
               description = "Returns the current progress of a bulk generation job")
    public ResponseEntity<?> getProgress(@PathVariable String jobId) {
        log.debug("Received request for generation progress: {}", jobId);
        
        try {
            GenerationProgress progress = generationOrchestrator.getProgress(jobId);
            
            if (progress != null) {
                return ResponseEntity.ok(progress);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        Map.of("error", "Job not found", "jobId", jobId)
                );
            }
            
        } catch (Exception e) {
            log.error("Error retrieving generation progress for job {}", jobId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Failed to retrieve progress", "message", e.getMessage())
            );
        }
    }
    
    /**
     * Cancels an ongoing generation job.
     */
    @PostMapping("/cancel-generation/{jobId}")
    @Operation(summary = "Cancel generation job",
               description = "Cancels an ongoing bulk generation job")
    public ResponseEntity<?> cancelGeneration(@PathVariable String jobId) {
        log.info("Received request to cancel generation job: {}", jobId);
        
        try {
            generationOrchestrator.cancelGeneration(jobId);
            return ResponseEntity.ok(Map.of("message", "Generation job cancelled", "jobId", jobId));
            
        } catch (Exception e) {
            log.error("Error cancelling generation job {}", jobId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Failed to cancel job", "message", e.getMessage())
            );
        }
    }
}
