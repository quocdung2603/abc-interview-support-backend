package com.abc.question_service.service;

import com.abc.question_service.dto.GenerationReport;
import com.abc.question_service.dto.GenerationRequest;
import com.abc.question_service.entity.Field;
import com.abc.question_service.entity.Level;
import com.abc.question_service.entity.Question;
import com.abc.question_service.entity.QuestionType;
import com.abc.question_service.entity.Topic;
import com.abc.question_service.model.CombinationKey;
import com.abc.question_service.repository.FieldRepository;
import com.abc.question_service.repository.LevelRepository;
import com.abc.question_service.repository.QuestionRepository;
import com.abc.question_service.repository.QuestionTypeRepository;
import com.abc.question_service.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionGeneratorService {

    private final QuestionRepository questionRepository;
    private final FieldRepository fieldRepository;
    private final TopicRepository topicRepository;
    private final LevelRepository levelRepository;
    private final QuestionTypeRepository questionTypeRepository;
    private final DistributionStrategy distributionStrategy;
    private final QuestionContentGenerator contentGenerator;

    /**
     * Generates questions in bulk based on the request parameters
     */
    @Transactional
    public GenerationReport generateQuestions(GenerationRequest request) {
        GenerationReport report = new GenerationReport();
        report.setRequestedCount(request.getTargetCount());
        report.setStartTime(LocalDateTime.now());
        report.setGeneratedCount(0);
        report.setFailedCount(0);

        try {
            // Load all reference data
            log.info("Loading reference data...");
            List<Field> fields = loadFields();
            List<Topic> topics = loadTopics();
            List<Level> levels = loadLevels();
            List<QuestionType> questionTypes = loadQuestionTypes();

            if (fields.isEmpty() || topics.isEmpty() || levels.isEmpty() || questionTypes.isEmpty()) {
                report.addError("Missing reference data: fields, topics, levels, or question types");
                return finalizeReport(report);
            }

            // Calculate distribution
            log.info("Calculating distribution for {} questions...", request.getTargetCount());
            Map<CombinationKey, Integer> distribution = distributionStrategy.calculateDistribution(
                request.getTargetCount(),
                fields,
                topics,
                levels,
                questionTypes
            );

            if (distribution.isEmpty()) {
                report.addError("No valid combinations found for distribution");
                return finalizeReport(report);
            }

            log.info("Distribution calculated: {} combinations", distribution.size());

            // Generate questions in batches
            int totalGenerated = 0;
            int sequenceNumber = 0;
            List<Question> batch = new ArrayList<>();

            for (Map.Entry<CombinationKey, Integer> entry : distribution.entrySet()) {
                CombinationKey key = entry.getKey();
                int count = entry.getValue();

                // Find entities for this combination
                Field field = findFieldById(fields, key.getFieldId());
                Topic topic = findTopicById(topics, key.getTopicId());
                Level level = findLevelById(levels, key.getLevelId());
                QuestionType questionType = findQuestionTypeById(questionTypes, key.getQuestionTypeId());

                if (field == null || topic == null || level == null || questionType == null) {
                    report.addError("Missing entity for combination: " + key);
                    report.setFailedCount(report.getFailedCount() + count);
                    continue;
                }

                // Generate questions for this combination
                for (int i = 0; i < count; i++) {
                    try {
                        Question question = createQuestion(
                            field, topic, level, questionType,
                            request.getDefaultUserId(),
                            request.getDefaultApproverId(),
                            sequenceNumber++
                        );

                        batch.add(question);
                        report.incrementFieldDistribution(field.getName());

                        // Persist batch when it reaches the batch size
                        if (batch.size() >= request.getBatchSize()) {
                            persistBatch(batch);
                            totalGenerated += batch.size();
                            log.info("Persisted batch of {} questions. Total: {}", batch.size(), totalGenerated);
                            batch.clear();
                        }
                    } catch (Exception e) {
                        log.error("Error generating question: {}", e.getMessage());
                        report.addError("Failed to generate question: " + e.getMessage());
                        report.setFailedCount(report.getFailedCount() + 1);
                    }
                }
            }

            // Persist remaining questions
            if (!batch.isEmpty()) {
                persistBatch(batch);
                totalGenerated += batch.size();
                log.info("Persisted final batch of {} questions. Total: {}", batch.size(), totalGenerated);
            }

            report.setGeneratedCount(totalGenerated);
            log.info("Generation complete. Generated: {}, Failed: {}", 
                totalGenerated, report.getFailedCount());

        } catch (Exception e) {
            log.error("Error during question generation: {}", e.getMessage(), e);
            report.addError("Generation failed: " + e.getMessage());
        }

        return finalizeReport(report);
    }

    /**
     * Creates a single question with all required fields initialized
     */
    private Question createQuestion(
            Field field,
            Topic topic,
            Level level,
            QuestionType questionType,
            Long userId,
            Long approverId,
            int sequenceNumber) {

        Question question = new Question();
        
        // Set relationships
        question.setField(field);
        question.setTopic(topic);
        question.setLevel(level);
        question.setQuestionType(questionType);
        
        // Generate content
        String content = contentGenerator.generateQuestionContent(
            field, topic, level, questionType, sequenceNumber
        );
        String answer = contentGenerator.generateQuestionAnswer(content, questionType);
        
        question.setQuestionContent(content);
        question.setQuestionAnswer(answer);
        
        // Set user fields
        question.setUserId(userId);
        question.setApprovedBy(approverId);
        
        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        question.setCreatedAt(now);
        question.setApprovedAt(now);
        
        // Initialize metadata fields
        question.setStatus("APPROVED");
        question.setLanguage("en");
        question.setSimilarityScore(0.0);
        question.setUsefulVote(0);
        question.setUnusefulVote(0);
        
        return question;
    }

    /**
     * Persists a batch of questions to the database
     */
    private void persistBatch(List<Question> questions) {
        try {
            questionRepository.saveAll(questions);
            questionRepository.flush();
        } catch (Exception e) {
            log.error("Error persisting batch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to persist batch: " + e.getMessage(), e);
        }
    }

    private List<Field> loadFields() {
        return fieldRepository.findAll();
    }

    private List<Topic> loadTopics() {
        return topicRepository.findAll();
    }

    private List<Level> loadLevels() {
        return levelRepository.findAll();
    }

    private List<QuestionType> loadQuestionTypes() {
        return questionTypeRepository.findAll();
    }

    private Field findFieldById(List<Field> fields, Long id) {
        return fields.stream()
            .filter(f -> f.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    private Topic findTopicById(List<Topic> topics, Long id) {
        return topics.stream()
            .filter(t -> t.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    private Level findLevelById(List<Level> levels, Long id) {
        return levels.stream()
            .filter(l -> l.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    private QuestionType findQuestionTypeById(List<QuestionType> types, Long id) {
        return types.stream()
            .filter(t -> t.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    private GenerationReport finalizeReport(GenerationReport report) {
        report.setEndTime(LocalDateTime.now());
        report.setDuration(Duration.between(report.getStartTime(), report.getEndTime()));
        return report;
    }
}
