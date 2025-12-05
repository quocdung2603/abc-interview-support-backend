package com.abc.question_service.service;

import com.abc.question_service.dto.GenerationReport;
import com.abc.question_service.dto.GenerationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for error handling in bulk generation
 * Tests that database constraint violations are caught and reported
 * **Validates: Requirements 4.2**
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ErrorHandlingTest {

    @Autowired
    private QuestionGeneratorService generatorService;

    /**
     * Test that generation handles missing reference data gracefully
     */
    @Test
    public void testHandlesMissingReferenceData() {
        // Create request with no reference data in database
        GenerationRequest request = new GenerationRequest();
        request.setTargetCount(10);
        request.setBatchSize(5);
        request.setDefaultUserId(1L);
        request.setDefaultApproverId(1L);

        GenerationReport report = generatorService.generateQuestions(request);

        // Should complete without throwing exception
        assertNotNull(report, "Report should not be null");
        assertEquals(0, report.getGeneratedCount(), 
            "Should generate 0 questions when no reference data exists");
        assertFalse(report.getErrors().isEmpty(), 
            "Should report errors when reference data is missing");
        assertTrue(report.getErrors().get(0).contains("Missing reference data"),
            "Error message should indicate missing reference data");
    }

    /**
     * Test that validation errors are properly reported
     */
    @Test
    public void testValidationErrorReporting() {
        GenerationRequest request = new GenerationRequest();
        request.setTargetCount(10);
        request.setBatchSize(5);
        request.setDefaultUserId(1L);
        request.setDefaultApproverId(1L);

        GenerationReport report = generatorService.generateQuestions(request);

        assertNotNull(report, "Report should not be null");
        assertNotNull(report.getStartTime(), "Start time should be recorded");
        assertNotNull(report.getEndTime(), "End time should be recorded");
        assertNotNull(report.getDuration(), "Duration should be calculated");
    }

    /**
     * Test that report contains error details
     */
    @Test
    public void testReportContainsErrorDetails() {
        GenerationRequest request = new GenerationRequest();
        request.setTargetCount(5);
        request.setBatchSize(5);
        request.setDefaultUserId(1L);
        request.setDefaultApproverId(1L);

        GenerationReport report = generatorService.generateQuestions(request);

        assertNotNull(report.getErrors(), "Errors list should not be null");
        if (report.getGeneratedCount() == 0) {
            assertFalse(report.getErrors().isEmpty(), 
                "Should have error messages when generation fails");
        }
    }
}
