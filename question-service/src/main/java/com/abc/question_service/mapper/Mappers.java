package com.abc.question_service.mapper;

import com.abc.question_service.dto.*;
import com.abc.question_service.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface Mappers {
    Field toEntity(FieldRequest req);
    @Mapping(target = "id", source = "id")
    FieldResponse toResponse(Field entity);

    @Mapping(target = "field.id", source = "fieldId")
    Topic toEntity(TopicRequest req);
    @Mapping(target = "fieldId", source = "field.id")
    @Mapping(target = "fieldName", source = "field.name")
    TopicResponse toResponse(Topic entity);

    Level toEntity(LevelRequest req);
    LevelResponse toResponse(Level entity);

    QuestionType toEntity(QuestionTypeRequest req);
    QuestionTypeResponse toResponse(QuestionType entity);

    @Mapping(target = "topic.id", source = "topicId")
    @Mapping(target = "field.id", source = "fieldId")
    @Mapping(target = "level.id", source = "levelId")
    @Mapping(target = "questionType.id", source = "questionTypeId")
    @Mapping(target = "questionContent", source = "content")
    @Mapping(target = "questionAnswer", source = "answer")
    Question toEntity(QuestionRequest req);
    
    @Mapping(target = "topicId", source = "topic.id")
    @Mapping(target = "fieldId", source = "field.id")
    @Mapping(target = "levelId", source = "level.id")
    @Mapping(target = "questionTypeId", source = "questionType.id")
    @Mapping(target = "fieldName", source = "field.name", defaultValue = "Unknown Field")
    @Mapping(target = "topicName", source = "topic.name", defaultValue = "Unknown Topic")
    @Mapping(target = "levelName", source = "level.name", defaultValue = "Unknown Level")
    @Mapping(target = "questionTypeName", source = "questionType.name", defaultValue = "Unknown Type")
    QuestionResponse toResponse(Question entity);
    
    @Mapping(target = "question.id", source = "questionId")
    @Mapping(target = "questionType.id", source = "questionTypeId")
    Answer toEntity(AnswerRequest req);
    
    @Mapping(target = "questionId", source = "question.id")
    @Mapping(target = "questionTypeId", source = "questionType.id")
    @Mapping(target = "answerContent", source = "content")
    @Mapping(target = "isCorrect", source = "isCorrect")
    @Mapping(target = "similarityScore", source = "similarityScore")
    @Mapping(target = "isSampleAnswer", source = "isSampleAnswer")
    @Mapping(target = "orderNumber", source = "orderNumber")
    @Mapping(target = "usefulVote", source = "usefulVote")
    @Mapping(target = "unusefulVote", source = "unusefulVote")
    AnswerResponse toResponse(Answer entity);
}


