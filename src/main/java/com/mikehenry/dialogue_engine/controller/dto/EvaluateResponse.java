package com.mikehenry.dialogue_engine.controller.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EvaluateResponse(
        @JsonProperty("question") String question,
        @JsonProperty("answer") String answer,
        @JsonProperty("generatedAt") Instant generatedAt
) {
    public static EvaluateResponse of(String question, String answer) {
        return new EvaluateResponse(question, answer, Instant.now());
    }
}
