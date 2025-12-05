package com.abc.question_service.service;

import com.abc.question_service.dto.GenerationReport;
import com.abc.question_service.dto.GenerationRequest;
import com.abc.question_service.dto.FieldRequest;
import com.abc.question_service.dto.LevelRequest;
import com.abc.question_service.dto.QuestionTypeRequest;
import com.abc.question_service.dto.TopicRequest;
import com.abc.question_service.entity.Question;
import com.abc.question_service.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for QuestionGeneratorService
 * 
 * Tests multiple correctness properties:
 * - Property 1: Generated count matches target count
 * - Property 3: Topic-field relationship integrity
 * - Property 4: All foreign keys reference existing entities
 * - Property 5: Timestamp ordering
 * - Property 8: Initial field values are correctly set
 * - Property 9: Valid user and approver assignment
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class QuestionGeneratorServicePropertyTest {

    @Autowired
    private QuestionGeneratorService generatorService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private LevelRepository levelRepository;

    @Autowired
    private QuestionTypeRepository questionTypeRepository;

    private final Random random = new Random();
    private Long fieldId1, fieldId2;
    private Long topicId1, topicId2;
    private Long levelId1, levelId2;
    private Long questionTypeId1, questionTypeId2;

    @BeforeEach
    public void setUp() {
        // Clean up
        questionRepository.deleteAll();
        topicRepository.deleteAll();
        fieldRepository.deleteAll();
        levelRepository.deleteAll();
        questionTypeRepository.deleteAll();

        // Create test data
        FieldRequest fieldRequest1 = new FieldRequest();
        fieldRequest1.setName("Field1");
        fieldRequest1.setDescription("Test Field 1");
        fieldId1 = questionService.createField(fieldRequest1).getId();

        FieldRequest fieldRequest2 = new FieldRequest();
        fieldRequest2.setName("Field2");
        fieldRequest2.setDescription("Test Field 2");
        fieldId2 = questionService.createField(fieldRequest2).getId();

        TopicRequest topicRequest1 = new TopicRequest();
        topicRequest1.setName("Topic1");
        topicRequest1.setDescription("Test Topic 1");
        topicRequest1.setFieldId(fieldId1);
        topicId1 = questionService.createTopic(topicRequest1).getId();

        TopicRequest topicRequest2 = new TopicRequest();
        topicRequest2.setName("Topic2");
        topicRequest2.setDescription("Test Topic 2");
        topicRequest2.setFieldId(fieldId2);
        topicId2 = questionService.createTopic(topicRequest2).getId();

        LevelRequest levelRequest1 = new LevelRequest();
        levelRequest1.setName("Level1");
        levelRequest1.setDescription("Test Level 1");
        levelRequest1.setMinScore(0);
        levelRequest1.setMaxScore(50);
        levelId1 = questionService.createLevel(levelRequest1).getId();

        LevelRequest levelRequest2 = new LevelRequest();
        levelRequest2.setName("Level2");
        levelRequest2.setDescription("Test Level 2");
        levelRequest2.setMinScore(51);
        levelRequest2.setMaxScore(100);
        levelId2 = questionService.createLevel(levelRequest2).getId();

        QuestionTypeRequest typeRequest1 = new QuestionTypeRequest();
        typeRequest1.setName("Single Choice");
        typeRequest1.setDescription("Single Choice Type");
        questionTypeId1 = questionService.createQuestionType(typeRequest1).getId();

        QuestionTypeRequest typeRequest2 = new QuestionTypeRequest();
        typeRequest2.setName("Multiple Choice");
        typeRequest2.setDescription("Multiple Choice Type");
        questionTypeId2 = questionService.createQuestionType(typeRequest2).getId();
    }

    /**
     * Property 1: Generated count matches target count
     * For any valid target count, when the generator completes, 
     * the number of questions created should equal the target count.
     * **Validates: Requirements 1.1**
     */
    @RepeatedTest(10)
    public void testGeneratedCountMatchesTarget() {
        // Generate random target count (20-100 to keep tests fast)
        int targetCount = 20 + random.nextInt(81);

        GenerationRequest request = new GenerationRequest();
        request.setTargetCount(targetCount);
        request.setBatchSize(10);
        request.setDefaultUserId(1L);
        request.setDefaultApproverId(1L);

        GenerationReport report = generatorService.generateQuestions(request);

        assertEquals(targetCount, report.getGeneratedCount(),
            "Generated count should match target count");
        
        // Verify in database
        long dbCount = questionRepository.count();
        assertEquals(targetCount, dbCount,
            "Database should contain exactly the target number of questions");
    }

    /**
     * Property 3: Topic-field relationship integrity
     * For any generated question, the topic's fieldId should match the question's fieldId.
     * **Validates: Requirements 2.2**
     */
    @RepeatedTest(10)
    public void testTopicFieldRelationshipIntegrity() {
        int targetCount = 20 + random.nextInt(31);

        GenerationRequest request = new GenerationRequest();
        request.setTargetCount(targetCount);
        request.setBatchSize(10);
        request.setDefaultUserId(1L);
        request.setDefaultApproverId(1L);

        generatorService.generateQuestions(request);

        List<Question> questions = questionRepository.findAll();
        
        for (Question question : questions) {
            assertNotNull(question.getTopic(), "Topic should not be null");
            assertNotNull(question.getField(), "Field should not be null");
            assertNotNull(question.getTopic().getField(), "Topic's field should not be null");
            
            assertEquals(question.getField().getId(), question.getTopic().getField().getId(),
                String.format("Question's field ID (%d) should match topic's field ID (%d)",
                    question.getField().getId(), question.getTopic().getField().getId()));
        }
    }

    /**
     * Property 4: All foreign keys reference existing entities
     * For any generated question, the fieldId, topicId, levelId, and questionTypeId 
     * should all reference existing entities in their respective tables.
     * **Validates: Requirements 2.3, 2.4, 2.5, 2.6**
     */
    @RepeatedTest(10)
    public void testAllForeignKeysReferenceExistingEntities() {
        int targetCount = 20 + random.nextInt(31);

        GenerationRequest request = new GenerationRequest();
        request.setTargetCount(targetCount);
        request.setBatchSize(10);
        request.setDefaultUserId(1L);
        request.setDefaultApproverId(1L);

        generatorService.generateQuestions(request);

        List<Question> questions = questionRepository.findAll();
        
        // Get all valid IDs
        Set<Long> validFieldIds = fieldRepository.findAll().stream()
            .map(f -> f.getId()).collect(Collectors.toSet());
        Set<Long> validTopicIds = topicRepository.findAll().stream()
            .map(t -> t.getId()).collect(Collectors.toSet());
        Set<Long> validLevelIds = levelRepository.findAll().stream()
            .map(l -> l.getId()).collect(Collectors.toSet());
        Set<Long> validTypeIds = questionTypeRepository.findAll().stream()
            .map(qt -> qt.getId()).collect(Collectors.toSet());

        for (Question question : questions) {
            assertTrue(validFieldIds.contains(question.getField().getId()),
                "Field ID should reference existing field");
            assertTrue(validTopicIds.contains(question.getTopic().getId()),
                "Topic ID should reference existing topic");
            assertTrue(validLevelIds.contains(question.getLevel().getId()),
                "Level ID should reference existing level");
            assertTrue(validTypeIds.contains(question.getQuestionType().getId()),
                "Question type ID should reference existing question type");
        }
    }

    /**
     * Property 5: Timestamp ordering
     * For any generated question, the createdAt timestamp should be less than or equal 
     * to the approvedAt timestamp, and both should be non-null.
     * **Validates: Requirements 1.5**
     */
    @RepeatedTest(10)
    public void testTimestampOrdering() {
        int targetCount = 20 + random.nextInt(31);

        GenerationRequest request = new GenerationRequest();
        request.setTargetCount(targetCount);
        request.setBatchSize(10);
        request.setDefaultUserId(1L);
        request.setDefaultApproverId(1L);

        generatorService.generateQuestions(request);

        List<Question> questions = questionRepository.findAll();
        
        for (Question question : questions) {
            assertNotNull(question.getCreatedAt(), "CreatedAt should not be null");
            assertNotNull(question.getApprovedAt(), "ApprovedAt should not be null");
            
            assertTrue(
                question.getCreatedAt().isBefore(question.getApprovedAt()) ||
                question.getCreatedAt().isEqual(question.getApprovedAt()),
                "CreatedAt should be before or equal to ApprovedAt"
            );
        }
    }

    /**
     * Property 8: Initial field values are correctly set
     * For any generated question, usefulVote should equal 0, unusefulVote should equal 0, 
     * similarityScore should equal 0.0, status should equal "APPROVED", and language should equal "en".
     * **Validates: Requirements 5.1, 5.2, 5.3, 5.4, 5.5**
     */
    @RepeatedTest(10)
    public void testInitialFieldValuesAreCorrectlySet() {
        int targetCount = 20 + random.nextInt(31);

        GenerationRequest request = new GenerationRequest();
        request.setTargetCount(targetCount);
        request.setBatchSize(10);
        request.setDefaultUserId(1L);
        request.setDefaultApproverId(1L);

        generatorService.generateQuestions(request);

        List<Question> questions = questionRepository.findAll();
        
        for (Question question : questions) {
            assertEquals(0, question.getUsefulVote(),
                "UsefulVote should be initialized to 0");
            assertEquals(0, question.getUnusefulVote(),
                "UnusefulVote should be initialized to 0");
            assertEquals(0.0, question.getSimilarityScore(), 0.001,
                "SimilarityScore should be initialized to 0.0");
            assertEquals("APPROVED", question.getStatus(),
                "Status should be set to APPROVED");
            assertEquals("en", question.getLanguage(),
                "Language should be set to en");
        }
    }

    /**
     * Property 9: Valid user and approver assignment
     * For any generated question, userId and approvedBy should be non-null and positive.
     * **Validates: Requirements 1.4**
     */
    @RepeatedTest(10)
    public void testValidUserAndApproverAssignment() {
        int targetCount = 20 + random.nextInt(31);
        Long userId = 1L + random.nextInt(10);
        Long approverId = 1L + random.nextInt(10);

        GenerationRequest request = new GenerationRequest();
        request.setTargetCount(targetCount);
        request.setBatchSize(10);
        request.setDefaultUserId(userId);
        request.setDefaultApproverId(approverId);

        generatorService.generateQuestions(request);

        List<Question> questions = questionRepository.findAll();
        
        for (Question question : questions) {
            assertNotNull(question.getUserId(), "UserId should not be null");
            assertNotNull(question.getApprovedBy(), "ApprovedBy should not be null");
            assertTrue(question.getUserId() > 0, "UserId should be positive");
            assertTrue(question.getApprovedBy() > 0, "ApprovedBy should be positive");
            assertEquals(userId, question.getUserId(), "UserId should match request");
            assertEquals(approverId, question.getApprovedBy(), "ApprovedBy should match request");
        }
    }

    /**
     * Property 10: Generation report accuracy
     * For any generation run, the GenerationReport's generatedCount should equal 
     * the number of questions actually persisted to the database.
     * **Validates: Requirements 4.5**
     */
    @RepeatedTest(10)
    public void testGenerationReportAccuracy() {
        int targetCount = 20 + random.nextInt(31);

        GenerationRequest request = new GenerationRequest();
        request.setTargetCount(targetCount);
        request.setBatchSize(10);
        request.setDefaultUserId(1L);
        request.setDefaultApproverId(1L);

        GenerationReport report = generatorService.generateQuestions(request);

        long dbCount = questionRepository.count();
        
        assertEquals(report.getGeneratedCount().longValue(), dbCount,
            "Report's generated count should match database count");
        assertEquals(targetCount, report.getRequestedCount(),
            "Report should record the requested count");
        assertNotNull(report.getStartTime(), "Start time should be recorded");
        assertNotNull(report.getEndTime(), "End time should be recorded");
        assertNotNull(report.getDuration(), "Duration should be calculated");
    }
}
