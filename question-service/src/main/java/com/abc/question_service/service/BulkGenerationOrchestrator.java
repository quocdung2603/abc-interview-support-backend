package com.abc.question_service.service;

import com.abc.question_service.dto.*;
import com.abc.question_service.entity.*;
import com.abc.question_service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Orchestrator for bulk question generation.
 * Coordinates distribution calculation, content generation, and batch processing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BulkGenerationOrchestrator {
    
    private final DistributionCalculator distributionCalculator;
    private final QuestionContentGenerator contentGenerator;
    private final QuestionRepository questionRepository;
    private final FieldRepository fieldRepository;
    private final TopicRepository topicRepository;
    private final LevelRepository levelRepository;
    private final QuestionTypeRepository questionTypeRepository;
    
    // Track progress for async operations
    private final Map<String, GenerationProgress> progressMap = new ConcurrentHashMap<>();
    
    /**
     * Generates questions in bulk according to the request parameters.
     */
    @Transactional
    public GenerationResult generateQuestions(BulkGenerationRequest request) {
        String jobId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();
        
        log.info("Starting bulk generation job {}: {} questions, batch size {}",
                jobId, request.getTargetCount(), request.getBatchSize());
        
        GenerationResult result = GenerationResult.builder()
                .jobId(jobId)
                .requestedCount(request.getTargetCount())
                .generatedCount(0)
                .failedCount(0)
                .startTime(startTime)
                .errors(new ArrayList<>())
                .distributionByField(new HashMap<>())
                .distributionByLevel(new HashMap<>())
                .distributionByQuestionType(new HashMap<>())
                .build();
        
        try {
            // Calculate distribution
            List<QuestionCombination> combinations = distributionCalculator.calculateDistribution(
                    request.getTargetCount()
            );
            
            // Initialize progress tracking
            initializeProgress(jobId, request.getTargetCount(), combinations.size());
            
            // Generate questions for each combination
            Set<String> existingContent = new HashSet<>();
            int totalGenerated = 0;
            int totalFailed = 0;
            
            for (QuestionCombination combination : combinations) {
                try {
                    int generated = generateForCombination(
                            combination,
                            request,
                            existingContent,
                            jobId
                    );
                    
                    totalGenerated += generated;
                    
                    // Update distributions
                    updateDistributions(result, combination, generated);
                    
                } catch (Exception e) {
                    log.error("Failed to generate questions for combination: {}", combination.getKey(), e);
                    result.getErrors().add("Failed for combination " + combination.getKey() + ": " + e.getMessage());
                    totalFailed += combination.getQuestionCount();
                }
            }
            
            result.setGeneratedCount(totalGenerated);
            result.setFailedCount(totalFailed);
            result.setSuccess(totalGenerated > 0);
            
            // Mark progress as complete
            updateProgress(jobId, totalGenerated, request.getTargetCount(), "COMPLETED");
            
        } catch (Exception e) {
            log.error("Bulk generation failed for job {}", jobId, e);
            result.setSuccess(false);
            result.getErrors().add("Generation failed: " + e.getMessage());
            updateProgress(jobId, 0, request.getTargetCount(), "FAILED");
        }
        
        LocalDateTime endTime = LocalDateTime.now();
        result.setEndTime(endTime);
        result.setDuration(formatDuration(Duration.between(startTime, endTime)));
        
        log.info("Bulk generation job {} completed: {} generated, {} failed",
                jobId, result.getGeneratedCount(), result.getFailedCount());
        
        return result;
    }
    
    /**
     * Generates questions for a single combination.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected int generateForCombination(
            QuestionCombination combination,
            BulkGenerationRequest request,
            Set<String> existingContent,
            String jobId) {
        
        log.debug("Generating {} questions for combination: {}",
                combination.getQuestionCount(), combination.getKey());
        
        // Load entities
        Field field = fieldRepository.findById(combination.getFieldId())
                .orElseThrow(() -> new RuntimeException("Field not found: " + combination.getFieldId()));
        Topic topic = topicRepository.findById(combination.getTopicId())
                .orElseThrow(() -> new RuntimeException("Topic not found: " + combination.getTopicId()));
        Level level = levelRepository.findById(combination.getLevelId())
                .orElseThrow(() -> new RuntimeException("Level not found: " + combination.getLevelId()));
        QuestionType questionType = questionTypeRepository.findById(combination.getQuestionTypeId())
                .orElseThrow(() -> new RuntimeException("QuestionType not found: " + combination.getQuestionTypeId()));
        
        int generated = 0;
        List<Question> batch = new ArrayList<>();
        
        for (int i = 0; i < combination.getQuestionCount(); i++) {
            try {
                // Generate content
                QuestionContent content = contentGenerator.generateQuestion(
                        field, topic, level, questionType, existingContent
                );
                
                // Add to existing content set
                existingContent.add(content.getQuestionText());
                
                // Create question entity
                Question question = createQuestion(
                        content,
                        field, topic, level, questionType,
                        request
                );
                
                batch.add(question);
                generated++;
                
                // Save in batches
                if (batch.size() >= request.getBatchSize()) {
                    if (!request.getDryRun()) {
                        questionRepository.saveAll(batch);
                        questionRepository.flush();
                    }
                    log.debug("Saved batch of {} questions", batch.size());
                    batch.clear();
                }
                
                // Update progress
                updateProgressIncrement(jobId);
                
            } catch (Exception e) {
                log.warn("Failed to generate question {}/{} for combination {}: {}",
                        i + 1, combination.getQuestionCount(), combination.getKey(), e.getMessage());
            }
        }
        
        // Save remaining batch
        if (!batch.isEmpty() && !request.getDryRun()) {
            questionRepository.saveAll(batch);
            questionRepository.flush();
            log.debug("Saved final batch of {} questions", batch.size());
        }
        
        return generated;
    }
    
    /**
     * Creates a Question entity from generated content.
     */
    private Question createQuestion(
            QuestionContent content,
            Field field,
            Topic topic,
            Level level,
            QuestionType questionType,
            BulkGenerationRequest request) {
        
        Question question = new Question();
        question.setUserId(request.getDefaultUserId());
        question.setField(field);
        question.setTopic(topic);
        question.setLevel(level);
        question.setQuestionType(questionType);
        question.setQuestionContent(content.getQuestionText());
        question.setQuestionAnswer(content.getAnswerText());
        question.setStatus("APPROVED");
        question.setLanguage("en");
        question.setCreatedAt(LocalDateTime.now());
        question.setApprovedAt(LocalDateTime.now());
        question.setApprovedBy(request.getDefaultApproverId());
        question.setUsefulVote(0);
        question.setUnusefulVote(0);
        question.setSimilarityScore(0.0);
        
        return question;
    }
    
    /**
     * Updates distribution maps in the result.
     */
    private void updateDistributions(
            GenerationResult result,
            QuestionCombination combination,
            int generated) {
        
        result.getDistributionByField().merge(
                combination.getFieldName(), generated, Integer::sum
        );
        result.getDistributionByLevel().merge(
                combination.getLevelName(), generated, Integer::sum
        );
        result.getDistributionByQuestionType().merge(
                combination.getQuestionTypeName(), generated, Integer::sum
        );
    }
    
    /**
     * Initializes progress tracking for a job.
     */
    private void initializeProgress(String jobId, int totalQuestions, int totalCombinations) {
        GenerationProgress progress = GenerationProgress.builder()
                .jobId(jobId)
                .totalQuestions(totalQuestions)
                .processedQuestions(0)
                .percentage(0)
                .status("IN_PROGRESS")
                .failedCount(0)
                .currentBatch(0)
                .totalBatches(totalCombinations)
                .build();
        
        progressMap.put(jobId, progress);
    }
    
    /**
     * Updates progress for a job.
     */
    private void updateProgress(String jobId, int processed, int total, String status) {
        GenerationProgress progress = progressMap.get(jobId);
        if (progress != null) {
            progress.setProcessedQuestions(processed);
            progress.setPercentage((processed * 100) / total);
            progress.setStatus(status);
        }
    }
    
    /**
     * Increments progress by one question.
     */
    private void updateProgressIncrement(String jobId) {
        GenerationProgress progress = progressMap.get(jobId);
        if (progress != null) {
            int processed = progress.getProcessedQuestions() + 1;
            progress.setProcessedQuestions(processed);
            progress.setPercentage((processed * 100) / progress.getTotalQuestions());
            
            // Log progress at intervals
            if (processed % 100 == 0) {
                log.info("Job {} progress: {}/{} ({}%)",
                        jobId, processed, progress.getTotalQuestions(), progress.getPercentage());
            }
        }
    }
    
    /**
     * Gets the current progress for a job.
     */
    public GenerationProgress getProgress(String jobId) {
        return progressMap.get(jobId);
    }
    
    /**
     * Cancels an ongoing generation job.
     */
    public void cancelGeneration(String jobId) {
        GenerationProgress progress = progressMap.get(jobId);
        if (progress != null) {
            progress.setStatus("CANCELLED");
            log.info("Generation job {} cancelled", jobId);
        }
    }
    
    /**
     * Formats a duration into a human-readable string.
     */
    private String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        
        if (minutes > 0) {
            return String.format("%d minutes %d seconds", minutes, remainingSeconds);
        } else {
            return String.format("%d seconds", remainingSeconds);
        }
    }
}
