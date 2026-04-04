package com.mikehenry.dialogue_engine.domain.service;

public interface BiographyEvaluationService {

    /**
     * Evaluates a question against the stored user biography and returns an answer.
     *
     * @param question the question to answer based on biography data
     * @return the model's answer derived from the biography
     */
    String evaluate(String question);
}
