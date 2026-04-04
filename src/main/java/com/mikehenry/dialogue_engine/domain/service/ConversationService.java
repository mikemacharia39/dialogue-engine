package com.mikehenry.dialogue_engine.domain.service;

public interface ConversationService {

    /**
     * Sends a user message to the LLM and returns the complete generated reply.
     *
     * @param message the user's input message
     * @return the model's full response text
     */
    String chat(String message);

    /**
     * Sends a user message to the LLM and returns a lazy token iterable for streaming.
     * Tokens are yielded one at a time as the model generates them.
     *
     * @param message the user's input message
     * @return an Iterable of token strings
     */
    Iterable<String> streamChat(String message);
}
