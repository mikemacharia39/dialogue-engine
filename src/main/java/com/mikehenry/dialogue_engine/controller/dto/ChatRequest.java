package com.mikehenry.dialogue_engine.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatRequest(@JsonProperty("message") String message) {
}
