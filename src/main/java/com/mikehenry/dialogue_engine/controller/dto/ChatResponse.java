package com.mikehenry.dialogue_engine.controller.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatResponse(
        @JsonProperty("response") String response,
        @JsonProperty("model") String model,
        @JsonProperty("generatedAt") Instant generatedAt
) {
    public static ChatResponse of(String response) {
        return new ChatResponse(response, "TinyLlama-1.1B-Chat", Instant.now());
    }
}
