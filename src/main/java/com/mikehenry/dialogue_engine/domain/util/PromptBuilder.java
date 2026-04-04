package com.mikehenry.dialogue_engine.domain.util;

import org.springframework.stereotype.Component;

/**
 * Builds TinyLlama-compatible chat prompts for different use cases.
 * Uses the ChatML / TinyLlama instruction format:
 * {@code <|system|>\n{system}\n</s>\n<|user|>\n{user}\n</s>\n<|assistant|>}
 */
@Component
public class PromptBuilder {

    private static final String CHAT_SYSTEM_MESSAGE =
            "You are Dialogue, a knowledgeable and helpful AI assistant. " +
            "Respond clearly, concisely, and in a friendly tone.";

    private static final String EVALUATE_SYSTEM_MESSAGE =
            "You are a precise assistant. Answer questions strictly based on the biography provided. " +
            "The current year of this questions is 2026." +
            "If the answer cannot be found in the biography, say so clearly.";

    public String buildChatPrompt(String userMessage) {
        return "<|system|>\n" + CHAT_SYSTEM_MESSAGE + "\n</s>\n" +
               "<|user|>\n" + userMessage.trim() + "\n</s>\n" +
               "<|assistant|>\n";
    }

    public String buildBiographyEvaluationPrompt(String question, String biographyContent) {
        return "<|system|>\n" + EVALUATE_SYSTEM_MESSAGE + "\n</s>\n" +
               "<|user|>\n" +
               "Biography:\n" + biographyContent.trim() + "\n\n" +
               "Question: " + question.trim() + "\n</s>\n" +
               "<|assistant|>\n";
    }
}
