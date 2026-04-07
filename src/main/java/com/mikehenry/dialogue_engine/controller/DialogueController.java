package com.mikehenry.dialogue_engine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikehenry.dialogue_engine.controller.dto.ChatRequest;
import com.mikehenry.dialogue_engine.controller.dto.ChatResponse;
import com.mikehenry.dialogue_engine.controller.dto.ChatTokenEvent;
import com.mikehenry.dialogue_engine.controller.dto.EvaluateRequest;
import com.mikehenry.dialogue_engine.controller.dto.EvaluateResponse;
import com.mikehenry.dialogue_engine.domain.service.BiographyEvaluationService;
import com.mikehenry.dialogue_engine.domain.service.ConversationService;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/api")
public class DialogueController {

    private final ConversationService conversationService;
    private final BiographyEvaluationService biographyEvaluationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DialogueController(ConversationService conversationService,
                               BiographyEvaluationService biographyEvaluationService) {
        this.conversationService = conversationService;
        this.biographyEvaluationService = biographyEvaluationService;
    }

    /**
     * Accepts a user message and returns an AI-generated conversational response.
     *
     * POST /api/chat
     * Body: {"message": "Hello, how are you?"}
     */
    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        log.info("POST /api/chat — message length: {}", StringUtils.isNotBlank(request.message()) ? request.message().length() : 0);
        String response = conversationService.chat(request.message());
        return ResponseEntity.ok(ChatResponse.of(response));
    }

    /**
     * Streams an AI-generated response token-by-token using Server-Sent Events.
     * Each SSE event carries a single token: {"token": "..."}
     * The final event is: [DONE]
     *
     * POST /api/chat/stream
     * Body: {"message": "Hello, how are you?"}
     */
    @PostMapping(value = "/chat/stream", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody ChatRequest request) {
        log.info("POST /api/chat/stream — message length: {}", request.message() != null ? request.message().length() : 0);
        SseEmitter emitter = new SseEmitter(120_000L);

        Thread.ofVirtual().start(() -> {
            try {
                for (String token : conversationService.streamChat(request.message())) {
                    emitter.send(SseEmitter.event()
                            .data(objectMapper.writeValueAsString(new ChatTokenEvent(token))));
                }
                emitter.send(SseEmitter.event().data("[DONE]"));
                emitter.complete();
            } catch (Exception e) {
                log.error("Error during SSE streaming", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * Accepts a question and evaluates it against the stored user biography.
     *
     * POST /api/evaluate
     * Body: {"question": "Where did the user study?"}
     */
    @PostMapping(value = "/evaluate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EvaluateResponse> evaluate(@RequestBody EvaluateRequest request) {
        log.info("POST /api/evaluate — question: {}", request.question());
        String answer = biographyEvaluationService.evaluate(request.question());
        return ResponseEntity.ok(EvaluateResponse.of(request.question(), answer));
    }
}
