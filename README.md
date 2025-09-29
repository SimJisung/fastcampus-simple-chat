# Simple Chat

Spring AI를 활용한 RAG(Retrieval-Augmented Generation) 기반 채팅 애플리케이션입니다.

## 🚀 주요 기능

- **AI 채팅**: Ollama를 사용한 대화형 AI 채팅
- **스트리밍 응답**: 실시간 스트리밍 채팅 지원
- **감정 분석**: 사용자 메시지의 감정 상태 분석
- **RAG 지원**: 문서 기반 검색 증강 생성
- **메모리 관리**: 대화 기록 관리 및 컨텍스트 유지
- **CLI 인터페이스**: 명령줄 기반 채팅 인터페이스
- **Swagger UI**: API 문서화 및 테스트 인터페이스

## 🧱 아키텍처 개요

- **RagChatApplication**: Spring Boot 진입점으로 REST API와 CLI를 함께 구동합니다.
- **ChatController / SimpleChatController**: `/chat` 이하의 동기, 스트리밍, 감정 분석 및 단일 메시지 API를 제공합니다.
- **ChatService / RagChatService**: 공통 Spring AI `ChatClient`를 사용해 일반 대화와 RAG 기반 대화를 처리합니다.
- **SimpleChatConfig**: 어드바이저 Bean, 대화 메모리, CLI 모드용 `CommandLineRunner`를 등록합니다.
- **RagConfig**: 문서 리더/분할기/메타데이터 변환과 벡터 스토어, RAG 어드바이저를 구성합니다.

## 🛠 기술 스택

- **Java 21**
- **Spring Boot 3.5.4**
- **Spring AI 1.0.2**
- **Ollama** (로컬 AI 모델)
- **Elasticsearch** (벡터 스토어)
- **Apache Tika** (문서 처리)
- **SpringDoc OpenAPI** (API 문서화)

## 📋 사전 요구사항

- Java 21 이상
- Docker 및 Docker Compose
- Ollama 설치 및 실행

## 🚀 시작하기

### 1. Ollama 설치 및 모델 다운로드

```bash
# Ollama 설치 (macOS)
brew install ollama

# Ollama 서비스 시작
ollama serve

# 필요한 모델 다운로드
ollama pull gpt-oss:20b
ollama pull bge-m3
```

### 2. Elasticsearch 실행

```bash
# Docker Compose로 Elasticsearch 실행
docker-compose up -d
```

### 3. 애플리케이션 실행

```bash
# Gradle로 애플리케이션 실행
./gradlew bootRun
```

애플리케이션이 실행되면 다음 URL에서 접근할 수 있습니다:
- **API 서버**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html

## 📚 API 엔드포인트

### 채팅 API

#### 0. 단일 메시지 채팅
```http
GET /chat?message=무엇이든 물어보세요
```

간단한 프롬프트를 빠르게 확인할 때 유용한 테스트 엔드포인트입니다.

#### 1. 일반 채팅
```http
POST /chat/call
Content-Type: application/json

{
  "conversationId": "unique-conversation-id",
  "userPrompt": "안녕하세요!",
  "systemPrompt": "당신은 도움이 되는 AI 어시스턴트입니다.",
  "chatOptions": {
    "temperature": 0.7,
    "maxTokens": 1000
  }
}
```

#### 2. 스트리밍 채팅
```http
POST /chat/stream
Content-Type: application/json

{
  "conversationId": "unique-conversation-id",
  "userPrompt": "오늘 날씨가 어떤가요?",
  "systemPrompt": "당신은 날씨 전문가입니다."
}
```

#### 3. 감정 분석
```http
POST /chat/emotion
Content-Type: application/json

{
  "conversationId": "unique-conversation-id",
  "userPrompt": "오늘 정말 기분이 좋아요!"
}
```

**응답 예시:**
```json
{
  "emotion": "HAPPY",
  "reasons": ["긍정적인 표현", "기쁨을 나타내는 단어 사용"]
}
```

## ⚙️ 설정

### application.properties 주요 설정

```properties
# 애플리케이션 설정
spring.application.name=simple-chat
app.cli.enabled=true
app.cli.filter-expression=
app.etl.pipleline.init=true
app.vector-store.in-memory.enabled=false

# AI 모델 설정
spring.ai.model.chat=ollama
spring.ai.ollama.chat.options.model=gpt-oss:20b
spring.ai.ollama.init.pull-model-strategy=when_missing
spring.ai.model.embedding=ollama
spring.ai.ollama.embedding.options.model=bge-m3

# (선택) OpenAI 호환 모델 사용 시
#spring.ai.openai.api-key=your-api-key-here
spring.ai.openai.chat.base-url=https://models.github.ai/inference
spring.ai.openai.chat.completions-path=/chat/completions

# RAG 설정
app.rag.document-location-pattern=classpath:documents/**/*.{pdf,txt}

# Elasticsearch 설정
spring.elasticsearch.uris=http://localhost:9200
spring.ai.vectorstore.elasticsearch.initialize-schema=true
spring.ai.vectorstore.elasticsearch.index-name=spring-ai-document-index
spring.ai.vectorstore.elasticsearch.dimensions=1024
spring.ai.vectorstore.elasticsearch.similarity=cosine
```

### CLI 모드 활성화

CLI 모드를 사용하려면 `application.properties`에서 다음 설정을 확인하세요:

```properties
app.cli.enabled=true
```

CLI 모드에서는 터미널에서 직접 AI와 대화할 수 있습니다.

- `app.cli.filter-expression`: RAG 문서 검색에 사용할 Elasticsearch 필터 표현식을 지정합니다.
- `app.etl.pipleline.init`: 애플리케이션 기동 시 문서를 다시 색인할지 여부를 제어합니다. 최초 기동 후에는 `false`로 변경해 재처리를 건너뛸 수 있습니다.
- `app.vector-store.in-memory.enabled`: 통합 테스트나 데모 용도로 인메모리 벡터 스토어를 사용하려면 `true`로 전환합니다.

CLI 루프가 실행되는 동안에도 REST 엔드포인트는 계속 제공되지만, 서버 전용으로 실행하고 싶다면 `app.cli.enabled=false`로 비활성화하면 됩니다.

## 📁 프로젝트 구조

```
src/main/java/ai/dawn/simple_chat/
├── RagChatApplication.java          # Spring Boot 메인 애플리케이션
├── chat/
│   ├── ChatController.java          # POST 기반 REST API (동기/스트리밍/감정 분석)
│   └── SimpleChatController.java    # GET 기반 단일 메시지 테스트 엔드포인트
├── config/
│   └── SimpleChatConfig.java        # 공통 Bean 및 CLI 모드 구성
├── rag/
│   ├── RagConfig.java              # RAG 설정
│   └── LengthTextSplitter.java     # 텍스트 분할기
└── service/
    ├── ChatService.java            # 기본 채팅 서비스 및 감정 평가
    └── RagChatService.java         # RAG 채팅 서비스
```

## 🔧 개발

### 테스트 실행

```bash
# 모든 테스트 실행
./gradlew test

# 상세 정보와 함께 테스트 실행
./gradlew test --info
```

### 빌드

```bash
# JAR 파일 빌드
./gradlew bootJar

# 클린 빌드
./gradlew clean bootJar
```

## 📖 RAG 기능

이 애플리케이션은 RAG(Retrieval-Augmented Generation) 기능을 지원합니다:

1. **문서 로딩**: `src/main/resources/documents/` 폴더의 PDF, TXT 파일을 자동으로 로드
2. **텍스트 분할**: 문서를 적절한 크기로 분할
3. **메타데이터 강화**: 키워드 기반 메타데이터 추가
4. **벡터화**: Elasticsearch를 사용한 벡터 저장
5. **검색 증강**: 관련 문서를 검색하여 응답 생성

### 문서 색인 파이프라인 제어

- ETL이 처음 수행될 때는 `app.etl.pipleline.init=true`로 유지해 문서를 읽고 전처리한 뒤 벡터 스토어에 적재합니다.
- 색인이 완료된 후 빠른 기동이 필요하면 해당 값을 `false`로 바꿔 재색인을 건너뛰세요.
- Elasticsearch 대신 인메모리 벡터 스토어를 시험하고 싶다면 `app.vector-store.in-memory.enabled=true`로 설정합니다.
- CLI 모드에서 `app.cli.filter-expression`을 지정하면 벡터 검색 결과에 추가 필터를 적용해 답변 범위를 조정할 수 있습니다.

## 🤝 기여하기

1. 이 저장소를 포크합니다
2. 기능 브랜치를 생성합니다 (`git checkout -b feature/amazing-feature`)
3. 변경사항을 커밋합니다 (`git commit -m 'Add some amazing feature'`)
4. 브랜치에 푸시합니다 (`git push origin feature/amazing-feature`)
5. Pull Request를 생성합니다

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

## 🆘 문제 해결

### 일반적인 문제

1. **Ollama 연결 오류**: Ollama 서비스가 실행 중인지 확인하세요
2. **Elasticsearch 연결 오류**: Docker Compose로 Elasticsearch가 실행 중인지 확인하세요
3. **모델 다운로드 오류**: 필요한 모델이 Ollama에 설치되어 있는지 확인하세요

### 로그 확인

```bash
# 애플리케이션 로그 확인
./gradlew bootRun --info
```

## 📞 지원

문제가 발생하거나 질문이 있으시면 이슈를 생성해 주세요.
