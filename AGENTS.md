# Repository Guidelines

## Project Structure & Module Organization
The application is a Spring Boot 3.5 service under `src/main/java/ai/dawn/simple_chat`. `SimpleChatApplication` bootstraps the service, `chat/` hosts REST controllers for synchronous, streaming, and emotion endpoints, and `service/ChatService` centralizes calls into Spring AI. Shared configuration such as advisors and chat memory lives in `config/SimpleChatConfig`. Static assets and configuration files belong in `src/main/resources`; runtimes read chat-related settings from `application.properties`. Tests mirror the main tree under `src/test/java`, beginning with `SimpleChatApplicationTests`.

## Build, Test, and Development Commands
Run `./gradlew bootRun` to start the API on port 8080; Swagger is available at `/swagger-ui.html` when the app is up. Use `./gradlew test` for the JUnit suite and add `--info` locally when diagnosing failures. Build a distributable jar with `./gradlew bootJar`, or add `clean` when you need a fresh build output.

## Coding Style & Naming Conventions
Target Java 21 with four-space indentation and UTF-8 source files. Keep packages inside `ai.dawn.simple_chat` and group code by feature (`chat`, `config`, `service`). Favor descriptive method names (`evaluateEmotion`) and align DTO record names with the API payloads they represent. When adding configuration, follow the existing `Simple*` naming to signal shared infrastructure, and register new Spring beans through configuration classes instead of inline definitions.

## Testing Guidelines
All tests run on JUnit Jupiter via `spring-boot-starter-test`. Place unit tests alongside their counterparts in `src/test/java/ai/dawn/simple_chat/...` and name them `*Tests`. Use `@WebMvcTest` or mocked `ChatClient` slices for controller coverage, falling back to `@SpringBootTest` only when wiring advisors end-to-end. Ensure new endpoints validate prompts, streaming behavior, and structured emotion responses before raising a PR, and execute `./gradlew test` prior to pushing.

## Commit & Pull Request Guidelines
This workspace currently lacks Git history, so adopt Conventional Commits (for example, `feat(chat): add memory window tuning`) to keep future history searchable. Each PR should include a concise summary, impacted endpoints or configuration flags, manual verification notes, and a link to the tracking issue. Attach JSON samples or curl commands whenever API contracts change, and confirm that tests pass in CI before requesting review.

## Configuration & Environment
Default runtime settings live in `src/main/resources/application.properties`. Ollama is the active chat provider (`gpt-oss:20b`) and pulls models on demand; switch to a remote provider by uncommenting the OpenAI-compatible keys and updating secrets. Toggle the optional CLI client with `spring.application.cli=true`, and keep the document loader pattern (`app.rag.document-location-pattern`) aligned with any future RAG assets you add under `resources/documents`.
