# Dialogue-Engine

Dialogue-Engine is a **Spring Boot REST API** that integrates with [llama.cpp](https://github.com/ggerganov/llama.cpp) (via the [`de.kherud:llama`](https://github.com/kherud/java-llama.cpp) Java bindings) to provide general-purpose conversational AI and biography-aware question evaluation — all running locally without any cloud dependency.

---

## Features

- **Conversational API** — Send user messages and receive AI-generated responses via `POST /api/chat`.
- **Streaming Chat** — Receive tokens in real time as they are generated via `POST /api/chat/stream` (Server-Sent Events).
- **Biography Evaluation Endpoint** — Submit a question and the engine evaluates it against a configurable user biography text file via `POST /api/evaluate`.
- **CLI Mode** — Chat interactively from the terminal with streaming token output, activated via the `cli` Spring profile.
- **Local Inference** — Uses llama.cpp under the hood for efficient, privacy-preserving local inference (no data leaves your machine).
- **TinyLlama Support** — Ships pre-configured for `TinyLlama-1.1B-Chat-v1.0.Q4_K_M.gguf` but works with any GGUF-format model.
- **Spring Boot 4 Backend** — Clean REST layer built on Spring MVC with constructor-injected services and configuration properties.

---

## Project Structure

```
dialogue-engine/
├── src/main/java/com/mikehenry/dialogue_engine/
│   ├── DialogueEngineApplication.java       # Entry point
│   ├── controller/
│   │   ├── DialogueController.java          # REST endpoints (/api/chat, /api/chat/stream, /api/evaluate)
│   │   └── dto/
│   │       ├── ChatRequest.java
│   │       ├── ChatResponse.java
│   │       ├── ChatTokenEvent.java
│   │       ├── EvaluateRequest.java
│   │       └── EvaluateResponse.java
│   ├── domain/
│   │   ├── service/
│   │   │   ├── ConversationService.java           # Chat service interface
│   │   │   ├── BiographyEvaluationService.java    # Evaluation service interface
│   │   │   └── impl/
│   │   │       ├── ConversationServiceImpl.java
│   │   │       └── BiographyEvaluationServiceImpl.java
│   │   └── util/
│   │       └── PromptBuilder.java           # TinyLlama prompt formatting
│   ├── cli/
│   │   └── CliConversationRunner.java       # Interactive CLI chat (@Profile("cli"))
│   └── config/
│       ├── LlamaProperties.java             # Typed configuration properties
│       └── LlamaInferenceEngine.java        # LLM lifecycle management (@PostConstruct/@PreDestroy)
├── src/main/resources/
│   ├── application.yaml
│   ├── application-cli.yaml                 # Disables web server for CLI mode
│   └── biography.txt                        # User biography data for evaluation
└── src/test/java/com/mikehenry/dialogue_engine/
    └── controller/
        ├── ChatEndpointTest.java            # MockMvc test for /api/chat
        ├── ChatStreamEndpointTest.java      # MockMvc SSE test for /api/chat/stream
        └── EvaluateEndpointTest.java        # MockMvc test for /api/evaluate
```

## 🗂️ System Architecture Overview

+-------------------+
|   User Request    |
+-------------------+
          |
          v
+-------------------+
| Spring Boot API   |
| (Dialogue-Engine) |
+-------------------+
          |
          v
+-------------------+
|   llama.cpp       |
| (Model Runtime)   |
+-------------------+
          |
          v
+---------------------------+
|   AI Model (GGUF format) |
| e.g., Mistral 7B, LLaMA 2 |
+---------------------------+
          |
          v
+-------------------+
|   Generated Text  |
+-------------------+


### 🔑 Flow Explanation
1. **User Request** → A client sends a message or question via your REST API.
2. **Spring Boot API** → Your backend receives the request and routes it to the appropriate service (chat or biography evaluation).
3. **llama.cpp Runtime** → Acts as the *engine room*, loading and running the chosen AI model locally.
4. **AI Model (GGUF)** → The actual large language model (e.g., Mistral, LLaMA 2) generates a response.
5. **Generated Text** → The output is returned to the user through your API.

---

👉 In short: *Dialogue-Engine is the interface, llama.cpp is the runtime engine, and the GGUF model is the brain.*


## Getting Started

### Prerequisites

- Java 21+
- Gradle 9+
- A GGUF model file (see [TheBloke on Hugging Face](https://huggingface.co/TheBloke) for options). 
  The application at the time of development is using [TinyLlama-1.1B-Chat](https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF).

### Installation

```bash
git clone https://github.com/your-username/dialogue-engine.git
cd dialogue-engine
./gradlew bootRun
```

The application starts on `http://localhost:8081` by default.

### Model Configuration

The default model path is set in `application.yaml`. Override it with an environment variable:

```bash
LLAMA_MODEL_PATH=/path/to/your/model.gguf ./gradlew bootRun
```

Or edit `application.yaml` directly:

```yaml
llama:
  model:
    path: /path/to/your/model.gguf
```

### Biography File

Place your biography data in `src/main/resources/biography.txt`. A sample file is included. Override the filename via:

```yaml
llama:
  biography:
    file: my-biography.txt
```

---

## CLI Mode

The application ships with an interactive terminal chat mode. It reuses the same model and services as the REST API — no extra setup needed.

Activate it with the `cli` Spring profile, which also disables the embedded web server:

```bash
# via Gradle (stdin is wired automatically)
./gradlew bootRun --args='--spring.profiles.active=cli'

The preferred approach is to build the project first and run the JAR directly. This ensures the CLI mode works as expected without Gradle's process management.

# via JAR (build first with ./gradlew bootJar)
java -jar build/libs/dialogue-engine-0.0.1-SNAPSHOT.jar --spring.profiles.active=cli
```

> **Note:** The `--args=` flag is Gradle-specific. When using `java -jar` directly, pass the profile flag without `--args=`.

**Example session:**

```
╔══════════════════════════════════════╗
║       Dialogue Engine  —  CLI        ║
╚══════════════════════════════════════╝
  Type your message and press Enter.
  Type 'exit' to quit.

You       > What is the capital of France?

Dialogue  > The capital of France is Paris, a city renowned for the
Eiffel Tower, world-class cuisine, and rich cultural heritage.

[Stats] promptTokens=38 | generatedTokens=24 | totalTokens=62 | promptChars=112 | avgCharsPerToken=3.8 | chunks=24 | timeElapsed=512ms | timeToFirstToken=145ms

You       > exit
Goodbye!
```

Tokens are printed to the console as they are generated. Stats are shown after each response. Type `exit` to end the session.

---

## API Endpoints

### Chat

```
POST /api/chat
Content-Type: application/json
```

**Request:**
```json
{
  "message": "Hello, how are you?"
}
```

**Response:**
```json
{
  "response": "I'm doing well, thank you for asking! How can I help you today?",
  "model": "TinyLlama-1.1B-Chat",
  "generatedAt": 1712227200000
}
```

---

### Streaming Chat

Streams the AI response token-by-token using [Server-Sent Events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events). The connection stays open until generation completes.

```
POST /api/chat/stream
Content-Type: application/json
Accept: text/event-stream
```

**Request:**
```json
{
  "message": "Tell me a joke"
}
```

**Response** (stream of SSE events):
```
data:{"token":"Why"}

data:{"token":" did"}

data:{"token":" the"}

data:{"token":" chicken"}

data:{"token":" cross"}

data:{"token":" the"}

data:{"token":" road?"}

data:[DONE]
```

**curl example:**
```bash
curl -N -X POST http://localhost:8081/api/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"message":"Tell me a joke"}' \
  --no-buffer
```

---

### Biography Evaluation

```
POST /api/evaluate
Content-Type: application/json
```

**Request:**
```json
{
  "question": "Where did the user study?"
}
```

**Response:**
```json
{
  "question": "Where did the user study?",
  "answer": "The user studied Computer Science at the University of Edinburgh and Machine Learning at University College London.",
  "generatedAt": 1712227205000
}
```

---

## Configuration Reference

All properties in `application.yaml` support environment variable overrides:

| Property               | Default                    | Description                     |
|------------------------|----------------------------|---------------------------------|
| `llama.model.path`     | *(required)*               | Path to the GGUF model file     |
| `llama.temperature`    | `0.7`                      | Sampling temperature            |
| `llama.top-p`          | `0.9`                      | Top-p nucleus sampling          |
| `llama.max-tokens`     | `512`                      | Maximum tokens to generate      |
| `llama.n-ctx`          | `2048`                     | Context window size             |
| `llama.threads`        | `4`                        | CPU threads for inference       |
| `llama.stop-strings`   | `["</s>", "<\|im_end\|>"]` | Sequences that stop generation  |
| `llama.biography.file` | `biography.txt`            | Biography file name (classpath) |

---

## Testing

```bash
./gradlew test
```

Tests use `@WebMvcTest` with mocked services — no model file needed to run them.

| Test class               | Covers                                                             |
|--------------------------|--------------------------------------------------------------------|
| `ChatEndpointTest`       | `POST /api/chat` — full JSON response                              |
| `ChatStreamEndpointTest` | `POST /api/chat/stream` — SSE token stream and `[DONE]` terminator |
| `EvaluateEndpointTest`   | `POST /api/evaluate` — biography question answering                |
