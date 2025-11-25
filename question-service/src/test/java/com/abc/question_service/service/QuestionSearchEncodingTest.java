package com.abc.question_service.service;

import com.abc.question_service.dto.QuestionResponse;
import com.abc.question_service.entity.*;
import com.abc.question_service.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: exam-question-api-fixes, Property 6: ID-based queries are encoding-independent
 * 
 * Tests for encoding-independent question search using numeric IDs.
 * Validates Requirements 4.2, 4.4, 4.5
 */
@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration"
})
@ActiveProfiles("test")
@Transactional
public class QuestionSearchEncodingTest {

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

    @Autowired
    private QuestionService questionService;

    private Random random = new Random();

    // Unicode test strings (Vietnamese, Chinese, Japanese, Arabic, Emoji)
    private static final String[] UNICODE_NAMES = {
        "Lập trình viên",
        "程序员",
        "プログラマー",
        "مبرمج",
        "Développeur 🚀",
        "Разработчик",
        "Προγραμματιστής",
        "개발자",
        "นักพัฒนา",
        "מתכנת"
    };

    @BeforeEach
    public void setup() {
        // Clean up before each test
        questionRepository.deleteAll();
        fieldRepository.deleteAll();
        topicRepository.deleteAll();
        levelRepository.deleteAll();
        questionTypeRepository.deleteAll();
    }

    /**
     * Property Test: For any field, topic, or level with Unicode characters in their names,
     * querying questions by their numeric IDs should return consistent results regardless
     * of the character encoding or database collation settings.
     */
    @Test
    public void testIdBasedSearchIsEncodingIndependent() {
        // Run property test with 100 iterations
        for (int iteration = 0; iteration < 100; iteration++) {
            // Create entities with Unicode names
            String unicodeName = UNICODE_NAMES[random.nextInt(UNICODE_NAMES.length)];
            
            Field field = new Field();
            field.setName("Field " + unicodeName + " " + iteration);
            field.setDescription("Description with Unicode: " + unicodeName);
            field = fieldRepository.save(field);
            
            Topic topic = new Topic();
            topic.setName("Topic " + unicodeName + " " + iteration);
            topic.setDescription("Description with Unicode: " + unicodeName);
            topic.setField(field);
            topic = topicRepository.save(topic);
            
            Level level = new Level();
            level.setName("Level " + unicodeName + " " + iteration);
            level.setDescription("Description with Unicode: " + unicodeName);
            level.setMinScore(0);
            level.setMaxScore(100);
            level = levelRepository.save(level);
            
            QuestionType questionType = new QuestionType();
            questionType.setName("Type " + unicodeName + " " + iteration);
            questionType.setDescription("Description with Unicode: " + unicodeName);
            questionType = questionTypeRepository.save(questionType);
            
            // Create questions with these entities
            int questionCount = 1 + random.nextInt(5);
            for (int i = 0; i < questionCount; i++) {
                Question question = new Question();
                question.setUserId(1L);
                question.setQuestionContent("Question " + i + " with Unicode: " + unicodeName);
                question.setQuestionAnswer("Answer with Unicode: " + unicodeName);
                question.setStatus("APPROVED");
                question.setLanguage("vi");
                question.setCreatedAt(LocalDateTime.now());
                question.setUsefulVote(0);
                question.setUnusefulVote(0);
                question.setField(field);
                question.setTopic(topic);
                question.setLevel(level);
                question.setQuestionType(questionType);
                questionRepository.save(question);
            }
            
            // Search by numeric IDs (should work regardless of Unicode in names)
            List<QuestionResponse> results1 = questionService.searchQuestionsByIds(
                field.getId(), Arrays.asList(topic.getId()), level.getId(), questionType.getId(), null);
            
            // Search again to verify consistency
            List<QuestionResponse> results2 = questionService.searchQuestionsByIds(
                field.getId(), Arrays.asList(topic.getId()), level.getId(), questionType.getId(), null);
            
            // Verify results are consistent
            assertEquals(questionCount, results1.size(),
                "Iteration " + iteration + ": First search should return " + questionCount + " questions");
            assertEquals(questionCount, results2.size(),
                "Iteration " + iteration + ": Second search should return " + questionCount + " questions");
            assertEquals(results1.size(), results2.size(),
                "Iteration " + iteration + ": Both searches should return same number of results");
            
            // Verify all results have the correct IDs
            for (QuestionResponse result : results1) {
                assertEquals(field.getId(), result.getFieldId(),
                    "Iteration " + iteration + ": Result should have correct field ID");
                assertEquals(topic.getId(), result.getTopicId(),
                    "Iteration " + iteration + ": Result should have correct topic ID");
                assertEquals(level.getId(), result.getLevelId(),
                    "Iteration " + iteration + ": Result should have correct level ID");
                assertEquals(questionType.getId(), result.getQuestionTypeId(),
                    "Iteration " + iteration + ": Result should have correct question type ID");
            }
            
            // Clean up for next iteration
            questionRepository.deleteAll();
            fieldRepository.deleteAll();
            topicRepository.deleteAll();
            levelRepository.deleteAll();
            questionTypeRepository.deleteAll();
        }
    }

    @Test
    public void testSearchWithVietnameseCharacters() {
        // Create entities with Vietnamese names
        Field field = createFieldWithName("Lập trình viên Java");
        Topic topic = createTopicWithName("Cơ sở dữ liệu", field);
        Level level = createLevelWithName("Trung cấp");
        QuestionType questionType = createQuestionTypeWithName("Trắc nghiệm");
        
        // Create questions
        for (int i = 0; i < 5; i++) {
            createQuestion(field, topic, level, questionType);
        }
        
        // Search by IDs
        List<QuestionResponse> results = questionService.searchQuestionsByIds(
            field.getId(), Arrays.asList(topic.getId()), level.getId(), questionType.getId(), null);
        
        assertEquals(5, results.size());
    }

    @Test
    public void testSearchWithChineseCharacters() {
        // Create entities with Chinese names
        Field field = createFieldWithName("程序员");
        Topic topic = createTopicWithName("数据库", field);
        Level level = createLevelWithName("中级");
        QuestionType questionType = createQuestionTypeWithName("选择题");
        
        // Create questions
        for (int i = 0; i < 3; i++) {
            createQuestion(field, topic, level, questionType);
        }
        
        // Search by IDs
        List<QuestionResponse> results = questionService.searchQuestionsByIds(
            field.getId(), Arrays.asList(topic.getId()), level.getId(), questionType.getId(), null);
        
        assertEquals(3, results.size());
    }

    @Test
    public void testSearchWithMixedUnicodeCharacters() {
        // Create entities with mixed Unicode characters
        Field field = createFieldWithName("Developer 🚀 Разработчик");
        Topic topic = createTopicWithName("Database 数据库 قاعدة البيانات", field);
        Level level = createLevelWithName("Intermediate 中级 متوسط");
        QuestionType questionType = createQuestionTypeWithName("Multiple Choice 选择题");
        
        // Create questions
        for (int i = 0; i < 7; i++) {
            createQuestion(field, topic, level, questionType);
        }
        
        // Search by IDs multiple times
        for (int i = 0; i < 10; i++) {
            List<QuestionResponse> results = questionService.searchQuestionsByIds(
                field.getId(), Arrays.asList(topic.getId()), level.getId(), questionType.getId(), null);
            
            assertEquals(7, results.size(), "Search iteration " + i + " should return consistent results");
        }
    }

    @Test
    public void testSearchWithNullIds() {
        // Create entities
        Field field = createFieldWithName("Test Field");
        Topic topic = createTopicWithName("Test Topic", field);
        Level level = createLevelWithName("Test Level");
        QuestionType questionType = createQuestionTypeWithName("Test Type");
        
        createQuestion(field, topic, level, questionType);
        
        // Search with null IDs (should return all approved questions)
        List<QuestionResponse> results = questionService.searchQuestionsByIds(null, null, null, null, null);
        
        assertTrue(results.size() >= 1);
    }

    @Test
    public void testSearchWithPartialIds() {
        // Create entities
        Field field = createFieldWithName("Lập trình");
        Topic topic = createTopicWithName("Java", field);
        Level level = createLevelWithName("Cao cấp");
        QuestionType questionType = createQuestionTypeWithName("Tự luận");
        
        createQuestion(field, topic, level, questionType);
        
        // Search with only field ID
        List<QuestionResponse> results1 = questionService.searchQuestionsByIds(
            field.getId(), null, null, null, null);
        assertTrue(results1.size() >= 1);
        
        // Search with field and topic IDs
        List<QuestionResponse> results2 = questionService.searchQuestionsByIds(
            field.getId(), Arrays.asList(topic.getId()), null, null, null);
        assertTrue(results2.size() >= 1);
    }

    private Field createFieldWithName(String name) {
        Field field = new Field();
        field.setName(name);
        field.setDescription("Description for " + name);
        return fieldRepository.save(field);
    }

    private Topic createTopicWithName(String name, Field field) {
        Topic topic = new Topic();
        topic.setName(name);
        topic.setDescription("Description for " + name);
        topic.setField(field);
        return topicRepository.save(topic);
    }

    private Level createLevelWithName(String name) {
        Level level = new Level();
        level.setName(name);
        level.setDescription("Description for " + name);
        level.setMinScore(0);
        level.setMaxScore(100);
        return levelRepository.save(level);
    }

    private QuestionType createQuestionTypeWithName(String name) {
        QuestionType questionType = new QuestionType();
        questionType.setName(name);
        questionType.setDescription("Description for " + name);
        return questionTypeRepository.save(questionType);
    }

    private Question createQuestion(Field field, Topic topic, Level level, QuestionType questionType) {
        Question question = new Question();
        question.setUserId(1L);
        question.setQuestionContent("Test Question " + random.nextInt(10000));
        question.setQuestionAnswer("Test Answer");
        question.setStatus("APPROVED");
        question.setLanguage("vi");
        question.setCreatedAt(LocalDateTime.now());
        question.setUsefulVote(0);
        question.setUnusefulVote(0);
        question.setField(field);
        question.setTopic(topic);
        question.setLevel(level);
        question.setQuestionType(questionType);
        return questionRepository.save(question);
    }
}
