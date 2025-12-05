package com.abc.question_service.service;

import com.abc.question_service.entity.Field;
import com.abc.question_service.entity.Level;
import com.abc.question_service.entity.QuestionType;
import com.abc.question_service.entity.Topic;
import org.junit.jupiter.api.RepeatedTest;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for QuestionContentGenerator
 * 
 * **Feature: question-bulk-generation, Property 2: All question content is unique**
 * For any set of generated questions, no two questions should have identical questionContent values.
 * **Validates: Requirements 1.2**
 * 
 * **Feature: question-bulk-generation, Property 7: Topic name appears in question content**
 * For any generated question, the questionContent should contain the topic name as a substring.
 * **Validates: Requirements 3.4**
 */
public class QuestionContentGeneratorPropertyTest {

    private final QuestionContentGenerator contentGenerator = new QuestionContentGenerator();
    private final Random random = new Random();

    /**
     * Property 2: All question content is unique
     * For any set of generated questions, no two questions should have identical questionContent values.
     */
    @RepeatedTest(100)
    public void testContentUniqueness() {
        // Generate test entities
        Field field = createField(1L, "TestField");
        Topic topic = createTopic(1L, "TestTopic", field);
        Level level = createLevel(1L, "TestLevel");
        QuestionType questionType = createQuestionType(1L, "Single Choice");

        // Generate multiple questions with different sequence numbers
        int numQuestions = 50 + random.nextInt(50); // 50-100 questions
        Set<String> generatedContent = new HashSet<>();

        for (int i = 0; i < numQuestions; i++) {
            String content = contentGenerator.generateQuestionContent(
                field, topic, level, questionType, i
            );

            assertFalse(generatedContent.contains(content),
                String.format("Duplicate content found: '%s'", content));
            
            generatedContent.add(content);
        }

        // Verify all content is unique
        assertEquals(numQuestions, generatedContent.size(),
            "All generated content should be unique");
    }

    /**
     * Property 2: Test uniqueness across different question types
     */
    @RepeatedTest(100)
    public void testContentUniquenessAcrossTypes() {
        Field field = createField(1L, "TestField");
        Topic topic = createTopic(1L, "TestTopic", field);
        Level level = createLevel(1L, "TestLevel");

        QuestionType singleChoice = createQuestionType(1L, "Single Choice");
        QuestionType multipleChoice = createQuestionType(2L, "Multiple Choice");
        QuestionType fillInBlank = createQuestionType(3L, "Fill in the Blank");

        Set<String> allContent = new HashSet<>();
        int questionsPerType = 20;

        // Generate questions for each type
        for (int i = 0; i < questionsPerType; i++) {
            String content1 = contentGenerator.generateQuestionContent(
                field, topic, level, singleChoice, i
            );
            String content2 = contentGenerator.generateQuestionContent(
                field, topic, level, multipleChoice, i
            );
            String content3 = contentGenerator.generateQuestionContent(
                field, topic, level, fillInBlank, i
            );

            allContent.add(content1);
            allContent.add(content2);
            allContent.add(content3);
        }

        // Verify all content is unique across types
        assertEquals(questionsPerType * 3, allContent.size(),
            "All generated content should be unique across question types");
    }

    /**
     * Property 7: Topic name appears in question content
     * For any generated question, the questionContent should contain the topic name as a substring.
     */
    @RepeatedTest(100)
    public void testTopicNameInContent() {
        // Generate random topic name
        String topicName = "Topic" + random.nextInt(1000);
        Field field = createField(1L, "TestField");
        Topic topic = createTopic(1L, topicName, field);
        Level level = createLevel(1L, "TestLevel");

        // Test with different question types
        QuestionType[] questionTypes = {
            createQuestionType(1L, "Single Choice"),
            createQuestionType(2L, "Multiple Choice"),
            createQuestionType(3L, "Fill in the Blank")
        };

        for (QuestionType questionType : questionTypes) {
            int sequenceNumber = random.nextInt(1000);
            String content = contentGenerator.generateQuestionContent(
                field, topic, level, questionType, sequenceNumber
            );

            assertTrue(content.contains(topicName),
                String.format("Question content should contain topic name '%s'. Content: '%s'",
                    topicName, content));
        }
    }

    /**
     * Property 7: Topic name appears in content for multiple topics
     */
    @RepeatedTest(100)
    public void testTopicNameInContentMultipleTopics() {
        Field field = createField(1L, "TestField");
        Level level = createLevel(1L, "TestLevel");
        QuestionType questionType = createQuestionType(1L, "Single Choice");

        // Generate multiple topics
        int numTopics = 5 + random.nextInt(10); // 5-15 topics
        for (int i = 0; i < numTopics; i++) {
            String topicName = "Topic" + i + "_" + random.nextInt(1000);
            Topic topic = createTopic((long) i, topicName, field);

            int sequenceNumber = random.nextInt(1000);
            String content = contentGenerator.generateQuestionContent(
                field, topic, level, questionType, sequenceNumber
            );

            assertTrue(content.contains(topicName),
                String.format("Question content should contain topic name '%s'. Content: '%s'",
                    topicName, content));
        }
    }

    /**
     * Test that answers are generated appropriately for each question type
     */
    @RepeatedTest(100)
    public void testAnswerGeneration() {
        QuestionType singleChoice = createQuestionType(1L, "Single Choice");
        QuestionType multipleChoice = createQuestionType(2L, "Multiple Choice");
        QuestionType fillInBlank = createQuestionType(3L, "Fill in the Blank");

        String content = "Sample question content";

        String answer1 = contentGenerator.generateQuestionAnswer(content, singleChoice);
        String answer2 = contentGenerator.generateQuestionAnswer(content, multipleChoice);
        String answer3 = contentGenerator.generateQuestionAnswer(content, fillInBlank);

        assertNotNull(answer1, "Single choice answer should not be null");
        assertNotNull(answer2, "Multiple choice answer should not be null");
        assertNotNull(answer3, "Fill in blank answer should not be null");

        assertFalse(answer1.isEmpty(), "Single choice answer should not be empty");
        assertFalse(answer2.isEmpty(), "Multiple choice answer should not be empty");
        assertFalse(answer3.isEmpty(), "Fill in blank answer should not be empty");
    }

    // Helper methods to create test entities
    private Field createField(Long id, String name) {
        Field field = new Field();
        field.setId(id);
        field.setName(name);
        return field;
    }

    private Topic createTopic(Long id, String name, Field field) {
        Topic topic = new Topic();
        topic.setId(id);
        topic.setName(name);
        topic.setField(field);
        return topic;
    }

    private Level createLevel(Long id, String name) {
        Level level = new Level();
        level.setId(id);
        level.setName(name);
        return level;
    }

    private QuestionType createQuestionType(Long id, String name) {
        QuestionType type = new QuestionType();
        type.setId(id);
        type.setName(name);
        return type;
    }
}
