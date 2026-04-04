package com.mikehenry.dialogue_engine.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatTokenEvent(@JsonProperty("token") String token) {
}
