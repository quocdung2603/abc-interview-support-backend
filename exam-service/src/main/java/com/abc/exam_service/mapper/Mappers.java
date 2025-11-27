package com.abc.exam_service.mapper;

import com.abc.exam_service.dto.*;
import com.abc.exam_service.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Mapper(componentModel = "spring")
public abstract class Mappers {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mapping(target = "topicIds", expression = "java(convertListToString(req.getTopicIds()))")
    @Mapping(target = "questionTypeIds", expression = "java(convertListToString(req.getQuestionTypeIds()))")
    public abstract Exam toEntity(ExamRequest req);
    
    @Mapping(target = "topicIds", expression = "java(convertStringToList(entity.getTopicIds()))")
    @Mapping(target = "questionTypeIds", expression = "java(convertStringToList(entity.getQuestionTypeIds()))")
    public abstract ExamResponse toResponse(Exam entity);

    @Mapping(target = "exam", ignore = true)
    public abstract ExamQuestion toEntity(ExamQuestionRequest req);
    @Mapping(target = "examId", source = "exam.id")
    public abstract ExamQuestionResponse toResponse(ExamQuestion entity);

    @Mapping(target = "exam", ignore = true)
    public abstract Result toEntity(ResultRequest req);
    @Mapping(target = "examId", source = "exam.id")
    public abstract ResultResponse toResponse(Result entity);

    @Mapping(target = "exam", ignore = true)
    public abstract UserAnswer toEntity(UserAnswerRequest req);
    @Mapping(target = "examId", source = "exam.id")
    public abstract UserAnswerResponse toResponse(UserAnswer entity);

    @Mapping(target = "exam", ignore = true)
    public abstract ExamRegistration toEntity(ExamRegistrationRequest req);
    @Mapping(target = "examId", source = "exam.id")
    public abstract ExamRegistrationResponse toResponse(ExamRegistration entity);


    protected String convertListToString(java.util.List<Long> list) {
        if (list == null) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
    
    protected java.util.List<Long> convertStringToList(String json) {
        if (json == null || json.isEmpty()) return java.util.List.of();
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, Long.class));
        } catch (JsonProcessingException e) {
            return java.util.List.of();
        }
    }
}
