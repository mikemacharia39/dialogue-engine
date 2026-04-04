package com.mikehenry.dialogue_engine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikehenry.dialogue_engine.controller.dto.EvaluateRequest;
import com.mikehenry.dialogue_engine.domain.service.BiographyEvaluationService;
import com.mikehenry.dialogue_engine.domain.service.ConversationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DialogueController.class)
class EvaluateEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ConversationService conversationService;

    @MockitoBean
    private BiographyEvaluationService biographyEvaluationService;

    @Test
    void evaluate_ShouldReturnAnswerWithEchoedQuestion() throws Exception {
        String question = "Where did the user study?";
        String answer = "The user studied Computer Science at the University of Edinburgh and Machine Learning at University College London.";
        when(biographyEvaluationService.evaluate(question)).thenReturn(answer);

        mockMvc.perform(post("/api/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EvaluateRequest(question))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.question").value(question))
                .andExpect(jsonPath("$.answer").value(answer))
                .andExpect(jsonPath("$.generatedAt").exists());
    }
}
