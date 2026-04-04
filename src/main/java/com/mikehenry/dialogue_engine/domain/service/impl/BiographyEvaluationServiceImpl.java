package com.mikehenry.dialogue_engine.domain.service.impl;

import com.mikehenry.dialogue_engine.config.LlamaInferenceEngine;
import com.mikehenry.dialogue_engine.config.LlamaProperties;
import com.mikehenry.dialogue_engine.domain.service.BiographyEvaluationService;
import com.mikehenry.dialogue_engine.domain.util.PromptBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class BiographyEvaluationServiceImpl implements BiographyEvaluationService {

    private final LlamaInferenceEngine inferenceEngine;
    private final PromptBuilder promptBuilder;
    private final LlamaProperties properties;
    private String biographyContent;

    public BiographyEvaluationServiceImpl(LlamaInferenceEngine inferenceEngine,
                                          PromptBuilder promptBuilder,
                                          LlamaProperties properties) {
        this.inferenceEngine = inferenceEngine;
        this.promptBuilder = promptBuilder;
        this.properties = properties;
    }

    @PostConstruct
    public void loadBiography() {
        String biographyFile = properties.getBiography().getFile();
        try {
            ClassPathResource resource = new ClassPathResource(biographyFile);
            biographyContent = resource.getContentAsString(StandardCharsets.UTF_8);
            log.info("Biography loaded from '{}'({} chars)", biographyFile, biographyContent.length());
        } catch (IOException e) {
            log.warn("Could not load biography file '{}': {}. Evaluation will use empty biography.", biographyFile, e.getMessage());
            biographyContent = "";
        }
    }

    @Override
    public String evaluate(String question) {
        log.debug("Evaluating question against biography: {}", question);
        String prompt = promptBuilder.buildBiographyEvaluationPrompt(question, biographyContent);
        String answer = inferenceEngine.generate(prompt);
        log.debug("Evaluation answer ({} chars)", answer.length());
        return answer;
    }
}
