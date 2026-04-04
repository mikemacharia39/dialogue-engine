package com.mikehenry.dialogue_engine.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EvaluateRequest(@JsonProperty("question") String question) {
}
