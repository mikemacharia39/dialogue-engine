package com.mikehenry.dialogue_engine.config;

import de.kherud.llama.InferenceParameters;
import de.kherud.llama.LlamaModel;
import de.kherud.llama.LlamaOutput;
import de.kherud.llama.ModelParameters;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.stream.StreamSupport;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LlamaInferenceEngine {

    private final LlamaProperties properties;
    private LlamaModel model;

    public LlamaInferenceEngine(LlamaProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void initialize() {
        String modelPath = properties.getModel().getPath();
        log.info("Loading LLM from: {}", modelPath);
        // Redirect stdout/stderr during model loading to suppress native library
        // loader messages (e.g. "Extracted 'libjllama.dylib'", "ggml-metal not found")
        // that bypass the Java logger and go directly to the system streams.
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        try {
            System.setOut(new PrintStream(OutputStream.nullOutputStream()));
            System.setErr(new PrintStream(OutputStream.nullOutputStream()));

            LlamaModel.setLogger(de.kherud.llama.args.LogFormat.TEXT, (level, msg) -> {});

            model = new LlamaModel(
                    new ModelParameters()
                            .setModel(modelPath)
                            .setThreads(properties.getThreads())
                            .setCtxSize(properties.getNCtx())
                            .disableLog()   // suppress slot/server diagnostic output
                            .disablePerf()  // suppress prompt/eval timing output
            );
        } catch (Exception e) {
            System.setOut(originalOut);
            System.setErr(originalErr);
            log.error("Failed to initialize LLM model from path '{}': {}", modelPath, e.getMessage());
            return;
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
        log.info("Model loaded successfully");
    }

    @PreDestroy
    public void shutdown() {
        if (model != null) {
            log.info("Shutting down LLM model");
            model.close();
        }
    }

    /**
     * Generates a complete text response for the given prompt by accumulating all output tokens.
     *
     * @param prompt the formatted prompt to send to the model
     * @return the generated text, trimmed of leading/trailing whitespace
     * @throws IllegalStateException if the model failed to load during startup
     */
    public String generate(String prompt) {
        if (model == null) {
            throw new IllegalStateException(
                    "LLM model is not available. Check that the model path is correct and the model loaded successfully.");
        }

        InferenceParameters params = new InferenceParameters(prompt)
                .setTemperature((float) properties.getTemperature())
                .setTopP((float) properties.getTopP())
                .setNPredict(properties.getMaxTokens())
                .setStopStrings(properties.getStopStrings().toArray(new String[0]));

        StringBuilder response = new StringBuilder();
        for (LlamaOutput token : model.generate(params)) {
            response.append(token.text);
        }
        return response.toString().trim();
    }

    /**
     * Returns a lazy token-by-token iterable suitable for streaming responses.
     * Each element is the raw text of one generated token.
     *
     * @param prompt the formatted prompt to send to the model
     * @return an Iterable that yields tokens one at a time as the model generates them
     * @throws IllegalStateException if the model failed to load during startup
     */
    public Iterable<String> streamTokens(String prompt) {
        if (model == null) {
            throw new IllegalStateException(
                    "LLM model is not available. Check that the model path is correct and the model loaded successfully.");
        }

        InferenceParameters params = new InferenceParameters(prompt)
                .setTemperature((float) properties.getTemperature())
                .setTopP((float) properties.getTopP())
                .setNPredict(properties.getMaxTokens())
                .setStopStrings(properties.getStopStrings().toArray(new String[0]));

        return () -> StreamSupport.stream(model.generate(params).spliterator(), false)
                .map(output -> output.text)
                .iterator();
    }

    /**
     * Returns the number of tokens the model encodes the given text into.
     * Returns 0 if the model is not loaded.
     */
    public int countTokens(String text) {
        if (model == null) return 0;
        return model.encode(text).length;
    }

    public boolean isReady() {
        return model != null;
    }
}
