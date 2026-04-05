package com.mikehenry.dialogue_engine.domain.service.impl;

import com.mikehenry.dialogue_engine.config.LlamaInferenceEngine;
import com.mikehenry.dialogue_engine.domain.service.ConversationService;
import com.mikehenry.dialogue_engine.domain.util.PromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class ConversationServiceImpl implements ConversationService {

    private final LlamaInferenceEngine inferenceEngine;
    private final PromptBuilder promptBuilder;

    @Override
    public String chat(String message) {
        log.debug("Processing chat message: {}", message);
        String prompt = promptBuilder.buildChatPrompt(message);
        String response = inferenceEngine.generate(prompt);
        log.debug("Generated chat response ({} chars)", response.length());
        return response;
    }

    @Override
    public Iterable<String> streamChat(String message) {
        log.debug("Starting streaming chat for message: {}", message);
        String prompt = promptBuilder.buildChatPrompt(message);
        return inferenceEngine.streamTokens(prompt);
    }
}
