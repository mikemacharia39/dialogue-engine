package com.mikehenry.dialogue_engine.cli;

import java.util.Scanner;

import com.mikehenry.dialogue_engine.config.LlamaInferenceEngine;
import com.mikehenry.dialogue_engine.domain.service.ConversationService;
import com.mikehenry.dialogue_engine.domain.util.PromptBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Activates an interactive CLI chat session when the application is run with the "cli" profile.
 * Tokens stream to the console in real time, and per-response stats are printed after each reply.
 *
 * Run with:
 *   ./gradlew bootRun --args='--spring.profiles.active=cli'
 * or:
 *   java -jar build/libs/dialogue-engine-0.0.1-SNAPSHOT.jar --spring.profiles.active=cli
 */
@Slf4j
@Profile("cli")
@Component
public class CliConversationRunner implements CommandLineRunner {

    private static final String PROMPT_USER = "You       > ";
    private static final String PROMPT_BOT  = "Dialogue  > ";
    private static final String EXIT_COMMAND = "exit";

    private final ConversationService conversationService;
    private final PromptBuilder promptBuilder;
    private final LlamaInferenceEngine inferenceEngine;

    public CliConversationRunner(ConversationService conversationService,
                                 PromptBuilder promptBuilder,
                                 LlamaInferenceEngine inferenceEngine) {
        this.conversationService = conversationService;
        this.promptBuilder = promptBuilder;
        this.inferenceEngine = inferenceEngine;
    }

    @Override
    public void run(String... args) {
        printWelcome();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print(PROMPT_USER);
                System.out.flush();

                if (!scanner.hasNextLine()) {
                    break;
                }

                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase(EXIT_COMMAND)) {
                    System.out.println("Goodbye!");
                    break;
                }

                if (input.length() < 2) {
                    continue;
                }

                System.out.println();
                System.out.print(PROMPT_BOT);
                System.out.flush();

                try {
                    String fullPrompt = promptBuilder.buildChatPrompt(input);
                    int promptTokens = inferenceEngine.countTokens(fullPrompt);
                    int promptChars = fullPrompt.length();

                    long startTime = System.currentTimeMillis();
                    long timeToFirstToken = -1;
                    int generatedTokens = 0;
                    int generatedChars = 0;

                    for (String token : conversationService.streamChat(input)) {
                        if (timeToFirstToken == -1) {
                            timeToFirstToken = System.currentTimeMillis() - startTime;
                        }
                        System.out.print(token);
                        System.out.flush();
                        generatedTokens++;
                        generatedChars += token.length();
                    }

                    long timeElapsed = System.currentTimeMillis() - startTime;
                    int totalTokens = promptTokens + generatedTokens;
                    double avgCharsPerToken = generatedTokens > 0
                            ? (double) generatedChars / generatedTokens : 0;

                    System.out.println();
                    System.out.printf(
                            "%n[Stats] promptTokens=%d | generatedTokens=%d | totalTokens=%d" +
                            " | promptChars=%d | avgCharsPerToken=%.1f | chunks=%d" +
                            " | timeElapsed=%dms | timeToFirstToken=%dms%n",
                            promptTokens, generatedTokens, totalTokens,
                            promptChars, avgCharsPerToken, generatedTokens,
                            timeElapsed, timeToFirstToken == -1 ? 0 : timeToFirstToken
                    );

                } catch (Exception e) {
                    log.error("Error generating response", e);
                    System.out.println("[error: could not generate a response]");
                }

                System.out.println();
            }
        }

        // Force JVM exit — native llama.cpp threads are non-daemon and would
        // otherwise keep the process alive after the chat session ends.
        System.exit(0);
    }

    private void printWelcome() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║       Dialogue Engine  —  CLI        ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println("  Type your message and press Enter.");
        System.out.println("  Type 'exit' to quit.");
        System.out.println();
    }
}
