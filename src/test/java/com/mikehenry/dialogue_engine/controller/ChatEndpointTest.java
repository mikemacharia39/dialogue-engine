package com.mikehenry.dialogue_engine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikehenry.dialogue_engine.controller.dto.ChatRequest;
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
class ChatEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ConversationService conversationService;

    @MockitoBean
    private BiographyEvaluationService biographyEvaluationService;

    @Test
    void chat_ShouldReturnResponseWithModelAndTimestamp() throws Exception {
        String userMessage = "What is the capital of France?";
        String modelReply = "The capital of France is Paris.";
        when(conversationService.chat(userMessage)).thenReturn(modelReply);

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChatRequest(userMessage))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value(modelReply))
                .andExpect(jsonPath("$.model").value("TinyLlama-1.1B-Chat"))
                .andExpect(jsonPath("$.generatedAt").exists());
    }
}
