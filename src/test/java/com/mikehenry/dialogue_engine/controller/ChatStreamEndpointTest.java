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
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DialogueController.class)
class ChatStreamEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ConversationService conversationService;

    @MockitoBean
    private BiographyEvaluationService biographyEvaluationService;

    @Test
    void streamChat_ShouldStreamTokensAndEndWithDone() throws Exception {
        String userMessage = "Tell me a joke";
        when(conversationService.streamChat(userMessage))
                .thenReturn(List.of("Why ", "did ", "the ", "chicken ", "cross ", "the ", "road?"));

        MvcResult asyncResult = mockMvc.perform(post("/api/chat/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChatRequest(userMessage))))
                .andExpect(request().asyncStarted())
                .andReturn();

        String body = mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).contains("\"token\":\"Why \"")
                .contains("\"token\":\"chicken \"")
                .contains("data:[DONE]");
    }
}
