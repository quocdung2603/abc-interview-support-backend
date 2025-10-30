package com.abc.question_service.controller;

import com.abc.question_service.dto.AnswerRequest;
import com.abc.question_service.config.JwtAuthenticationFilter;
import com.abc.question_service.config.JwtConfig;
import com.abc.question_service.config.SecurityConfig;
import com.abc.question_service.service.QuestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = QuestionController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtConfig.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
class QuestionControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuestionService questionService;

    @Test
    void createAnswer_missingQuestionId_returnsBadRequest() throws Exception {
        AnswerRequest req = new AnswerRequest();
        req.setUserId(1L);
        // req.setQuestionId(null); // missing
        req.setQuestionTypeId(2L);
        req.setContent("Some answer");

        mockMvc.perform(post("/questions/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
