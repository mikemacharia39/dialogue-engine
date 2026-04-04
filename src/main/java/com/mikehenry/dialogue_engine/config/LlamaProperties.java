package com.mikehenry.dialogue_engine.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "llama")
public class LlamaProperties {

    private Model model = new Model();
    private double temperature = 0.7;
    private double topP = 0.9;
    private int maxTokens = 512;
    private int nCtx = 2048;
    private int threads = 4;
    private List<String> stopStrings = new ArrayList<>(List.of("</s>", "<|im_end|>"));
    private Biography biography = new Biography();

    @Setter
    @Getter
    public static class Model {
        private String path = "model/default.gguf";

    }

    @Setter
    @Getter
    public static class Biography {
        private String file = "biography.txt";

    }
}
