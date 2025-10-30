package com.abc.question_service.controller;

import com.abc.question_service.dto.*;
import com.abc.question_service.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
@Tag(name = "Question Service", description = "APIs for managing questions, fields, topics, levels, types, and answers")
public class QuestionController {
    private final QuestionService svc;

    // ==================== FIELDS ====================
    
    @PostMapping("/fields")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new field", description = "Create a new field (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Field created successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public FieldResponse createField(@RequestBody FieldRequest req) { 
        return svc.createField(req); 
    }
    
    @GetMapping("/fields")
    @Operation(summary = "Get all fields", description = "Retrieve paginated list of all fields")
    @ApiResponse(responseCode = "200", description = "Fields retrieved successfully")
    public Page<FieldResponse> getAllFields(
        @Parameter(description = "Pagination parameters") Pageable pageable) { 
        return svc.getAllFields(pageable); 
    }
    
    @GetMapping("/fields/{id}")
    @Operation(summary = "Get field by ID", description = "Retrieve a specific field by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Field found"),
        @ApiResponse(responseCode = "404", description = "Field not found")
    })
    public FieldResponse getFieldById(
        @Parameter(description = "Field ID") @PathVariable Long id) { 
        return svc.getFieldById(id); 
    }
    
    @PutMapping("/fields/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update field", description = "Update an existing field (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Field updated successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Field not found")
    })
    public FieldResponse updateField(
        @Parameter(description = "Field ID") @PathVariable Long id, 
        @RequestBody FieldRequest req) { 
        return svc.updateField(id, req); 
    }
    
    @DeleteMapping("/fields/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete field", description = "Delete a field (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Field deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Field not found")
    })
    public void deleteField(
        @Parameter(description = "Field ID") @PathVariable Long id) { 
        svc.deleteField(id); 
    }
    
    // ==================== TOPICS ====================
    
    // ==================== TOPICS ====================
    
    @PostMapping("/topics")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new topic", description = "Create a new topic under a field (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Topic created successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public TopicResponse createTopic(@RequestBody TopicRequest req) { 
        return svc.createTopic(req); 
    }
    
    @GetMapping("/topics")
    @Operation(summary = "Get all topics", description = "Retrieve paginated list of all topics with field names")
    @ApiResponse(responseCode = "200", description = "Topics retrieved successfully")
    public Page<TopicResponse> getAllTopics(
        @Parameter(description = "Pagination parameters") Pageable pageable) { 
        return svc.getAllTopics(pageable); 
    }
    
    @GetMapping("/topics/{id}")
    @Operation(summary = "Get topic by ID", description = "Retrieve a specific topic by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Topic found"),
        @ApiResponse(responseCode = "404", description = "Topic not found")
    })
    public TopicResponse getTopicById(
        @Parameter(description = "Topic ID") @PathVariable Long id) { 
        return svc.getTopicById(id); 
    }
    
    @PutMapping("/topics/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update topic", description = "Update an existing topic (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Topic updated successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Topic not found")
    })
    public TopicResponse updateTopic(
        @Parameter(description = "Topic ID") @PathVariable Long id, 
        @RequestBody TopicRequest req) { 
        return svc.updateTopic(id, req); 
    }
    
    @DeleteMapping("/topics/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete topic", description = "Delete a topic (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Topic deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Topic not found")
    })
    public void deleteTopic(
        @Parameter(description = "Topic ID") @PathVariable Long id) { 
        svc.deleteTopic(id); 
    }
    
    // ==================== LEVELS ====================
    
    @PostMapping("/levels")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new level", description = "Create a new difficulty level (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public LevelResponse createLevel(@RequestBody LevelRequest req) { 
        return svc.createLevel(req); 
    }
    
    @GetMapping("/levels")
    @Operation(summary = "Get all levels", description = "Retrieve paginated list of all difficulty levels")
    public Page<LevelResponse> getAllLevels(
        @Parameter(description = "Pagination parameters") Pageable pageable) { 
        return svc.getAllLevels(pageable); 
    }
    
    @GetMapping("/levels/{id}")
    @Operation(summary = "Get level by ID", description = "Retrieve a specific level by its ID")
    public LevelResponse getLevelById(
        @Parameter(description = "Level ID") @PathVariable Long id) { 
        return svc.getLevelById(id); 
    }
    
    @PutMapping("/levels/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update level", description = "Update an existing level (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public LevelResponse updateLevel(
        @Parameter(description = "Level ID") @PathVariable Long id, 
        @RequestBody LevelRequest req) { 
        return svc.updateLevel(id, req); 
    }
    
    @DeleteMapping("/levels/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete level", description = "Delete a level (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public void deleteLevel(
        @Parameter(description = "Level ID") @PathVariable Long id) { 
        svc.deleteLevel(id); 
    }
    
    // ==================== QUESTION TYPES ====================
    
    @PostMapping("/question-types")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create question type", description = "Create a new question type (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public QuestionTypeResponse createQuestionType(@RequestBody QuestionTypeRequest req) { 
        return svc.createQuestionType(req); 
    }
    
    @GetMapping("/question-types")
    @Operation(summary = "Get all question types", description = "Retrieve paginated list of all question types")
    public Page<QuestionTypeResponse> getAllQuestionTypes(
        @Parameter(description = "Pagination parameters") Pageable pageable) { 
        return svc.getAllQuestionTypes(pageable); 
    }
    
    @GetMapping("/question-types/{id}")
    @Operation(summary = "Get question type by ID", description = "Retrieve a specific question type by its ID")
    public QuestionTypeResponse getQuestionTypeById(
        @Parameter(description = "Question Type ID") @PathVariable Long id) { 
        return svc.getQuestionTypeById(id); 
    }
    
    @PutMapping("/question-types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update question type", description = "Update an existing question type (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public QuestionTypeResponse updateQuestionType(
        @Parameter(description = "Question Type ID") @PathVariable Long id, 
        @RequestBody QuestionTypeRequest req) { 
        return svc.updateQuestionType(id, req); 
    }
    
    @DeleteMapping("/question-types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete question type", description = "Delete a question type (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public void deleteQuestionType(
        @Parameter(description = "Question Type ID") @PathVariable Long id) { 
        svc.deleteQuestionType(id); 
    }

    // ==================== QUESTIONS ====================
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public QuestionResponse createQuestion(@Valid @RequestBody QuestionRequest req) { return svc.createQuestion(req); }

    @GetMapping
    public Page<QuestionResponse> getAllQuestions(Pageable pageable) { 
        return svc.getAllQuestions(pageable); 
    }
    
    @GetMapping("/{id:[0-9]+}")
    public QuestionResponse getQuestionById(@PathVariable Long id) { return svc.getQuestionById(id); }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public QuestionResponse updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionRequest req) { 
        return svc.updateQuestion(id, req); 
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteQuestion(@PathVariable Long id) { 
        svc.deleteQuestion(id); 
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public QuestionResponse approve(@PathVariable Long id, @RequestParam Long adminId) { return svc.approveQuestion(id, adminId); }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public QuestionResponse reject(@PathVariable Long id, @RequestParam Long adminId) { return svc.rejectQuestion(id, adminId); }
    
    @GetMapping("/topics/{topicId}/questions")
    public Page<QuestionResponse> listByTopic(@PathVariable Long topicId, Pageable pageable) { 
        return svc.listQuestionsByTopic(topicId, pageable); 
    }

    // Answers
    @PostMapping("/answers")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public AnswerResponse createAnswer(@Valid @RequestBody AnswerRequest req) { return svc.createAnswer(req); }

    @GetMapping("/answers")
    public Page<AnswerResponse> getAllAnswers(Pageable pageable) { return svc.getAllAnswers(pageable); }
    
    @GetMapping("/answers/{id}")
    public AnswerResponse getAnswerById(@PathVariable Long id) { return svc.getAnswerById(id); }
    
    @PutMapping("/answers/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public AnswerResponse updateAnswer(@PathVariable Long id, @Valid @RequestBody AnswerRequest req) { return svc.updateAnswer(id, req); }

    @DeleteMapping("/answers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteAnswer(@PathVariable Long id) { svc.deleteAnswer(id); }

    @PostMapping("/answers/{id}/sample")
    @PreAuthorize("hasRole('ADMIN')")
    public AnswerResponse markSample(@PathVariable Long id, @RequestParam boolean isSample) { return svc.markSampleAnswer(id, isSample); }

    @GetMapping("/{questionId}/answers")
    public Page<AnswerResponse> listAnswers(@PathVariable Long questionId, Pageable pageable) { return svc.listAnswersByQuestion(questionId, pageable); }
}